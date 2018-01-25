# android letters

[![](https://jitpack.io/v/keygenqt/android-select-file.svg)](https://jitpack.io/#keygenqt/android-select-file)

Add it to your build.gradle with:
```gradle
allprojects {
    repositories {
        // ...
        maven { url "https://jitpack.io" }
    }
}
```
and:

```gradle
dependencies {
    compile 'com.github.keygenqt:android-select-file:version'
}
```

## Usage

```xml
<color name="general_color">#42A1D8</color>
```

```java
new SelectFile((ActivitySelect) getView().getActivity(), new SelectFileProperties()
        .setChoosePhoto(false)
        .setMaxFilesCount(5)
        .setMimes(new ArrayList<String>() {{
            add(SelectFileMime.MIME_DOC);
            add(SelectFileMime.MIME_PDF);
            add(SelectFileMime.MIME_PPT);
            add(SelectFileMime.MIME_TXT);
            add(SelectFileMime.MIME_XLS);
            add(SelectFileMime.MIME_ZIP);
            add(SelectFileMime.MIME_RAR);
        }}))
        .show(files -> {

        }, error -> SettingsBase.getBase().getShowMessage().call(error));

new SelectFile((ActivitySelect) getView().getActivity(), new SelectFileProperties()
        .setChoosePhoto(true)
        .setMaxFilesCount(1)
        .setMimes(new ArrayList<String>() {{
            add(SelectFileMime.MIME_JPG);
            add(SelectFileMime.MIME_PNG);
        }})).show(files -> {

        }, error -> SettingsBase.getBase().getShowMessage().call(error));
```

## Screenshot

![Alt text](https://raw.githubusercontent.com/keygenqt/android-letters/master/screenshot/1.png "View")
