package com.kazurayam.materials

interface ExecutionProfile extends Comparable<ExecutionProfile> {

    String getName()

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
    String getNameInPathSafeChars()

}
