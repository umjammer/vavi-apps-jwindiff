[![Release](https://jitpack.io/v/umjammer/vavi-apps-jwindiff.svg)](https://jitpack.io/#umjammer/vavi-apps-jwindiff)
[![Java CI](https://github.com/umjammer/vavi-apps-jwindiff/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-apps-jwindiff/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/vavi-apps-jwindiff/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/vavi-apps-jwindiff/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-8-b07219)

# JWinDiff

🐣 Incubation for swing bindings.

## TODO

 * ~~change JFileChooserTextField's TextField to ComboBox~~
 * ~~ComboBox w/ history~~
 * ~~popup menu for ComboBox~~
 * saving history
 * ~~JFontChooser~~
 * ~~directory sorting~~
 * ~~opoup menu~~
 * alignment for JCheckBoxMenuItem and normal JMeniItem → [EmptyIcon.java](justsystem/ark11src/jp/co/justsystem/uiparts/EmptyIcon.java)
 * InputMap [see](https://web.archive.org/web/20090110193923/http://www.hcn.zaq.ne.jp/no-ji/reseach/20000206.htm)
 * Moved line
 * separate graphics Left files and Right files
 * ~~matching regex is something wrong~~
 * default focus for a dialog
 * default button behaviour is something wrong
 * list to table
 * ~~DnD to JFileChooserHistoryComboBox [Refference](https://web.archive.org/web/20010127050300/http://www5.big.or.jp/~tera/Labo/Java2/j2dnd.html)~~
 * MVC separation perfectly

## Tech Know

 * JPopupMenu didn't work because of [this](http://developer.java.sun.com/developer/bugParade/bugs/4632782.html)
 * File#getCanonicalPath is huge cost
