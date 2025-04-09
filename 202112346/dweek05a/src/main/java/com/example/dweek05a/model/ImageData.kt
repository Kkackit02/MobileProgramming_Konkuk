package com.example.dweek05a.model

import androidx.compose.runtime.saveable.listSaver

data class ImageData(
    val image: ImageUri,
    val buttonType: ButtonType = ButtonType.ICON,
    //buttonType은 기본값 지정

    var likes: Int = 0,
    var dislikes: Int = 0
) {
    companion object {
        //복원할 타입이 앞으로
        val ImageSaver = listSaver<ImageData, Any>(
            // list로 저장한다는거니까..

            //저장할때는 int, string, 등등 각종 데이터로 저장
            save = { item ->
                listOf(
                    when (item.image) {
                        is ImageUri.ResImage -> item.image.resID
                        is ImageUri.WebImage -> item.image.webUrl
                        else -> throw IllegalArgumentException("타입 오류")
                    },
                    item.buttonType.name,
                    item.likes,
                    item.dislikes
                )
            },

            //출력할때는 다시 ImageData에 맞게
            // 다시 resImage, webImage, Button등에 맞게 변환해서 내보내줘야함
            restore = { list ->
                val img = list[0]
                val image = when (img) {
                    is Int -> ImageUri.ResImage(img)
                    is String -> ImageUri.WebImage(img)
                    else -> throw IllegalArgumentException("타입 오류")
                }
                // data가 다 준비되었으니
                // ImageData로 변환

                ImageData(
                    image = image,
                    buttonType = ButtonType.valueOf(list[1] as String),
                    //String을 Enum으로 변환해줌
                    likes = list[2] as Int,
                    dislikes = list[3] as Int
                )

            }
        )
    }
}


