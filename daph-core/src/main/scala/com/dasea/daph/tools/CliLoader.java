package com.dasea.daph.tools;

import com.sampullara.cli.Argument;

public class CliLoader {
    @Argument(alias = "st", description = "Daph json格式配置内容的存储地类型，目前支持local-fs/mysql/minio，可通过实现JsonLoader接口，自定义类型")
    public static String daphJsonStorageType;

    @Argument(alias = "sc", description = "Daph json格式配置内容的存储地配置，配置形式为 key1=value1,key2=value2,,,")
    public static String[] daphJsonStorageConfig = new String[]{};
    @Argument(alias = "job", description = "Daph Job配置文件路径")
    public static String daphJobJsonPath;

    @Argument(alias = "computer", description = "Daph Computer配置文件路径")
    public static String daphComputerJsonPath;

    @Argument(alias = "storage", description = "Daph Storage配置文件路径")
    public static String daphStorageJsonPath;

    @Argument(alias = "executor", description = "Daph Executor配置文件路径")
    public static String daphExecutorJsonPath;

    @Argument(alias = "dudps", description = "命令行自定义参数，配置形式为 key1=value1,key2=value2,,,")
    public static String[] daphUserDefinedParameters = new String[]{};
}
