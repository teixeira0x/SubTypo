Most of the code in this module is inspired by [subtitleedit](https://github.com/SubtitleEdit/subtitleedit/tree/main/src/libse)

## Idea

**SubtitleView.kt**

It is a view to draw custom text for a caption paragraph.  

**/formats/SubtitleFormat.kt**

A base class for subtitle formats. 

**/models/Subtitle.kt**

This class manipulates the subtitle file, has methods for adding, updating and removing paragraphs, and manages the class and paragraph states for ``Undo/Redo`` actions using the [Memento Pattern](https://en.wikipedia.org/wiki/Memento_pattern). 