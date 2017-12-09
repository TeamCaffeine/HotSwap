package com.teamcaffeine.hotswap.image;

/**
 * ImagePickerWithCrop Library from Mickael First
 * https://github.com/Tofira/ImagePickerWithCrop
 */

public class GlobalHolder {

    private PickerManager pickerManager;

    private static GlobalHolder ourInstance = new GlobalHolder();

    public static GlobalHolder getInstance() {
        return ourInstance;
    }

    private GlobalHolder() {
    }


    public PickerManager getPickerManager() {
        return pickerManager;
    }

    public void setPickerManager(PickerManager pickerManager) {
        this.pickerManager = pickerManager;
    }
}
