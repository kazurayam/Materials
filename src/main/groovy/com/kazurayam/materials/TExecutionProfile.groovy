package com.kazurayam.materials

import java.nio.file.Path

final class TExecutionProfile implements Comparable<TExecutionProfile> {

    static final String UNUSED_DIRNAME = '_'
    static final TExecutionProfile UNUSED = TExecutionProfile.newInstance(UNUSED_DIRNAME)

    static final TExecutionProfile BLANK = TExecutionProfile.newInstance('')

    private String profileName_

    TExecutionProfile(String profileName) {
        Objects.requireNonNull(profileName, "profileName must not be null")
        profileName_ = profileName
    }

    /**
     * Given a path of a directory immediately under the Materials or the Storage directory,
     * instantiates a TExecutionProfile object
     *
     * @param folder
     */
    TExecutionProfile(Path folder) {
        String folderName = folder.getFileName().toString()
        StringBuilder sb = new StringBuilder()
        char[] chars = folderName.toCharArray()
        for (int i = 0; i < chars.length; i++) {
            boolean foundInPairs = false
            for (pair in charAndPathSafeEscapePairs) {
                if (chars[i] == pair.get(1)) {
                    sb.append(pair.get(0))
                    foundInPairs = true
                }
            }
            if ( !foundInPairs ) {
                sb.append(chars[i])
            }
        }
        this.profileName_ = sb.toString()
    }

    String getName() {
        return profileName_
    }

    private static List<Tuple2<Character, Character>> charAndPathSafeEscapePairs

    static {
        charAndPathSafeEscapePairs = new ArrayList<Tuple2<Character, Character>>()
        def p = charAndPathSafeEscapePairs
        p.add(new Tuple2('\\', '￥'))
        p.add(new Tuple2('/',  '／'))
        p.add(new Tuple2(':',  '：'))
        p.add(new Tuple2('*',  '＊'))
        p.add(new Tuple2('?',  '？'))
        p.add(new Tuple2('\"', '”'))
        p.add(new Tuple2('<',  '＜'))
        p.add(new Tuple2('>',  '＞'))
        p.add(new Tuple2('|',  '｜'))
    }

    /**
     * On Windows file system there are a set of characters which
     * are not allowed to be part of file name or folder name.
     *
     * \ / : * ? " < > |
     *
     * this method replaces those special character to those
     * which are safe as a file/folder name component.
     *
     * @return
     */
    String getNameInPathSafeChars() {
        StringBuilder sb = new StringBuilder()
        char[] chars = this.getName().toCharArray()
        for (int i = 0; i < chars.length; i++) {
            boolean foundInPairs = false
            for (pair in charAndPathSafeEscapePairs) {
                if (pair.get(0) == chars[i]) {
                    sb.append(pair.get(1))
                    foundInPairs = true
                }
            }
            if ( !foundInPairs ) {
                sb.append(chars[i])
            }
        }
        return sb.toString()
    }

    @Override
    String toString() {
        return profileName_
    }

    @Override
    public boolean equals(Object obj) {
        //if (this == obj)
        //    return true
        if (!(obj instanceof TExecutionProfile))
            return false
        TExecutionProfile other = (TExecutionProfile)obj
        return this.getName() == other.getName()
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode()
    }

    @Override
    int compareTo(TExecutionProfile other) {
        return this.getName() <=> other.getName()
    }

}
