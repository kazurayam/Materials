package com.kazurayam.materials

final class TExecutionProfile implements Comparable<TExecutionProfile> {

    static final TExecutionProfile BLANK = TExecutionProfile.newInstance('')

    private String profileName_

    TExecutionProfile(String profileName) {
        Objects.requireNonNull(profileName, "profileName must not be null")
        profileName_ = profileName
    }

    String getName() {
        return profileName_
    }

    /**
     * Windowsのファイルシステムでファイル名ないしディレクトリ名として
     * 使ってはいけないcharすなわち
     *
     * \ / : * ? " < > |
     *
     * を安全な別のcharに置換してnameを返す。
     *
     * @return
     */
    String getNameInPathSafeChars() {
        StringBuilder sb = new StringBuilder()
        char[] chars = this.getName().toCharArray()
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '\\' :
                    sb.append('￥')
                    break
                case '/' :
                    sb.append('／')
                    break
                case ':' :
                    sb.append('：')
                    break
                case '*' :
                    sb.append('＊')
                    break
                case '?' :
                    sb.append('？')
                    break
                case '\"' :
                    sb.append('”')
                    break
                case '<' :
                    sb.append('＜')
                    break
                case '>' :
                    sb.append('＞')
                    break
                case '|' :
                    sb.append('｜')
                    break
                default:
                    sb.append(chars[i])
                    break
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
