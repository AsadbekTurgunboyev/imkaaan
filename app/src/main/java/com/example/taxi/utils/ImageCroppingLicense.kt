package com.example.taxi.utils

import com.app.imagepickerlibrary.ImagePicker
import com.app.imagepickerlibrary.model.PickExtension

fun createImagePicker(): ImagePicker {
 val img : ImagePicker by lazy {
     createImagePicker()
 }
    img.title("My Picker")
        .multipleSelection(enable = false, maxCount = 5)
        .showCountInToolBar(false)
        .showFolder(true)
        .cameraIcon(true)
        .doneIcon(true)
        .allowCropping(false)
        .compressImage(false)
        .maxImageSize(2)
        .extension(PickExtension.JPEG)

    return img
}