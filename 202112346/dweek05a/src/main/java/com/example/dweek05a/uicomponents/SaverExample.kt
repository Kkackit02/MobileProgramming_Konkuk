
package com.example.dweek05a.uicomponents

import android.os.Parcelable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import kotlinx.parcelize.Parcelize

@Parcelize
data class City(val name: String, val country: String) : Parcelable

@Composable
fun CityScreen(modifier: Modifier = Modifier) {
    var selectedCity by rememberSaveable {
        mutableStateOf(City("Madrid", "Spain"))
    }
    Text("${selectedCity.name} ${selectedCity.country}")
}

data class City2(val name: String, val country: String) {
    companion object {
        val nameKey = "Name"
        val countryKey = "Country"

        val cityMapSaver = mapSaver(
            save = { mapOf(nameKey to it.name, countryKey to it.country) },
            restore = {
                City2(it[nameKey] as String, it[countryKey] as String)
            }
        )
        //map 형태로 saver 구성

        val cityListSaver = listSaver<City2, Any>(
            save = { listOf(it.name, it.country) },
            restore = {
                City2(it[0] as String, it[1] as String)
                //list로 name, contry 인식
            }
        )

        //내가 원하는대로 saver를 구성하는 함수
        val citySaver = Saver<City2, Any>(
            save = {
                listOf(it.name, it.country)
            },
            restore = {
                val list = it as List<Any>
                City2(list[0] as String, list[1] as String)
            }
        )
    }
}

// --------------------------------------------------------------------------------


@Composable
fun CityScreen2(modifier: Modifier = Modifier) {
    var selectedCity by rememberSaveable(
        stateSaver = City2.cityMapSaver
        //map 세이버 지정
    ) {
        mutableStateOf(City2("Madrid", "Spain"))
        //이러면 이제 city2 객체의 데이터를 저장
    }

    Text("${selectedCity.name}\t${selectedCity.country}")
}

@Composable
fun CityScreen3(modifier: Modifier = Modifier) {
    var selectedCity by rememberSaveable(
        stateSaver = City2.cityListSaver
    ) {
        mutableStateOf(City2("Madrid", "Spain"))
    }

    Text("${selectedCity.name}\t${selectedCity.country}")
}

@Composable
fun CityScreen4(modifier: Modifier = Modifier) {
    var selectedCity by rememberSaveable(
        stateSaver = City2.citySaver
    ) {
        mutableStateOf(City2("Madrid", "Spain"))
    }

    Text("${selectedCity.name}\t${selectedCity.country}")
}

@Composable
fun CityScreen5(modifier: Modifier = Modifier) {
    val cityListSaver = listSaver<SnapshotStateList<City2>, Any>(
        // list를 반환받긴했는데
        // list 안에 list를 그대로 넣을 수 없어서
        // 하나의 list로 만들어주는 과정
        save = { list ->
            list.flatMap { city ->
                //list를 펼쳐주는 함수(list 안에 list들을 다 펼쳐줌
                listOf<Any>(
                    city.name, city.country
                )
            }
        },
        restore = { flat ->
            flat.chunked(2).map { (name, country) ->
                // 두 개 씩 묶어주기
                // 그래서 나온 name, country를 묶어서 만들어주기
                // 일단 flat 시키고 -> 다시 묶어주기
                City2(
                    name as String, country as String
                )
            }.toMutableStateList()
        }
    )
    val cityList = rememberSaveable(saver = cityListSaver) {
        mutableStateListOf<City2>(
            City2("Madrid", "Spain"), City2("ToKyo", "Japan"), City2("Seoul", "Korea")
        )
    }

    Column {
        cityList.forEach { city ->
            Text("${city.name}\t${city.country}")
        }
    }
}

@Composable
fun CityScreen6(modifier: Modifier = Modifier) {
    val cityMapSaver = mapSaver(
        // map 세이버는 list가 들어왔을 때,
        save = { list ->
            mapOf(
                "names" to list.map { it.name },
                // name에 해당하는 것들만 모아서 list를 만들기
                "countries" to list.map { it.country }
                // countrie에 해당하는 것들만 모아서 list 만들기
            )
        },
        restore = { map ->
            val names = map["names"] as List<String>
            val countries = map["countries"] as List<String>
            names.zip(countries)
                // name과 countrie를 묶어주기
                .map { (name, country) -> City2(name, country) }
                .toMutableStateList()
            // 그 후 mutable state list로 만들어서 반환

        }
    )

    // 근데 이런 과정이 너무 어렵고 귀찮자나..
    // 그래서 다음시간에 배울 VIEW 모델이라는것을 만들어서 쓸 것임.

    val cityList = rememberSaveable(saver = cityMapSaver) {
        mutableStateListOf<City2>(
            City2("Madrid", "Spain"), City2("ToKyo", "Japan"), City2("Seoul", "Korea")
        )
    }

    Column {
        cityList.forEach { city ->
            Text("${city.name}\t${city.country}")
        }
    }
}
