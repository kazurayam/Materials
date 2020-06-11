package com.kazurayam.materials.view

import com.kazurayam.materials.ExecutionProfile

final class ExecutionProfileImpl implements ExecutionProfile {
    
    public static final ExecutionProfile BLANK = ExecutionProfileImpl.newInstance('')
    
    private String profileName_
    
    private ExecutionProfileImpl(String profileName) {
        profileName_ = profileName
    }
    
    static ExecutionProfile newInstance(String profileName) {
        Objects.requireNonNull(profileName)
        return new ExecutionProfileImpl(profileName)
    }
    
    @Override
    String getName() {
        return profileName_
    }

    @Override
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
    
    // ---------------- overriding Object properties --------------------------
    @Override
    String toString() {
        return profileName_
    }
    
    @Override
    public boolean equals(Object obj) {
        //if (this == obj)
        //    return true
        if (!(obj instanceof ExecutionProfile))
            return false
        ExecutionProfile other = (ExecutionProfile)obj
        return this.getName().equals(other.getName())
    }
    
    @Override
    public int hashCode() {
        return this.getName().hashCode()
    }
    
    @Override
    int compareTo(ExecutionProfile other) {
        return this.getName().compareTo(other.getName())
    }
    
}
    