const { onRequest } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const axios = require("axios");

const openaiKey = defineSecret("OPENAI_API_KEY");

exports.recommendRoute = onRequest(
    { secrets: [openaiKey] },
    async (req, res) => {
        try {
            const { start, goal, candidateRoutesInfo } = req.body;

            if (!start || !goal || !candidateRoutesInfo || !Array.isArray(candidateRoutesInfo)) {
                return res.status(400).json({ error: "Missing or invalid candidateRoutesInfo." });
            }

            let prompt = `
You're a smart navigation assistant. A user is traveling from:
- Start: (${start.lat}, ${start.lng})
- Goal: (${goal.lat}, ${goal.lng})

We have explored several route options. Please analyze the following candidate routes and recommend the best one, or suggest new intermediate waypoints if none of the existing routes are ideal due to construction.

Here are the candidate routes:
`;

            candidateRoutesInfo.forEach((route, index) => {
                prompt += `
--- Candidate Route ${index + 1} (ID: ${route.id}) ---
  Length: ${route.lengthKm} km
  Construction Zones: ${route.hazardSites.length} zones
`;
                if (route.hazardSites.length > 0) {
                    prompt += `  Locations: ${route.hazardSites.map(s => `(${s.lat}, ${s.lng})`).join(', ')}
`;
                }
                if (route.routeCoordinates && route.routeCoordinates.length > 0) {
                    // Include all coordinates for detailed analysis
                    const coordsString = route.routeCoordinates.map(c => `(${c.lat},${c.lng})`).join(';');
                    prompt += `  Full Path Coordinates: ${coordsString}
`;
                }
            });

            prompt += `
Your task is to:
1. Analyze the geographical shape of each route using the provided full path coordinates, considering their relation to hazard sites. Select the safest and most efficient route.
2. If all existing routes have construction and you can suggest a better alternative, provide up to 2 intermediate waypoints (lat/lng) that help avoid these zones, following likely roads or real paths.

Respond ONLY with JSON in this format, with NO extra explanation:

Option 1: Recommend an existing route
{
  "decision": "choose_route",
  "chosenRouteIndex": [Index of the chosen route, 0-based]
}

Option 2: Suggest new waypoints
{
  "decision": "suggest_waypoints",
  "waypoints": [
    {"lat": ..., "lng": ...},
    {"lat": ..., "lng": ...}
  ]
}
`;

            const gptResponse = await axios.post(
                "https://api.openai.com/v1/chat/completions",
                {
                    model: "gpt-4",
                    temperature: 0.7, // Allow some creativity for waypoints
                    messages: [
                        { role: "system", content: "You are a smart navigation assistant." },
                        { role: "user", content: prompt }
                    ]
                },
                {
                    headers: {
                        Authorization: `Bearer ${openaiKey.value()}`,
                        "Content-Type": "application/json"
                    }
                }
            );

            const content = gptResponse.data.choices[0].message.content;
            console.log("GPT Raw Response:", content);

            const match = content.match(/\{[\s\S]*\}/);
            if (!match) {
                console.error("JSON block not found in GPT response");
                return res.status(500).json({ error: "Invalid GPT response format." });
            }

            const json = JSON.parse(match[0]);
            // Basic validation for GPT's response structure
            if (!json.decision || (json.decision === "choose_route" && (json.chosenRouteIndex === undefined || typeof json.chosenRouteIndex !== 'number')) || (json.decision === "suggest_waypoints" && (!json.waypoints || !Array.isArray(json.waypoints)))) {
                console.error("GPT response has invalid structure:", json);
                return res.status(500).json({ error: "Invalid GPT response structure." });
            }

            return res.status(200).json(json);

        } catch (e) {
            console.error("GPT request failed:", e.message);
            return res.status(500).json({ error: "GPT request failed" });
        }
    }
);