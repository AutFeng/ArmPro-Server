.class public LInvoketTest;
.super Ljava/lang/Object;
.source "InvoketTest.java"


# direct methods
.method public constructor <init>()V
    .registers 1

    .prologue
    .line 22
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static rc4(Ljava/lang/String;)Ljava/lang/String;
    .registers 16
    .param p0, "jiami"    # Ljava/lang/String;

    .prologue
    .line 64
    const-string v0, "ArmVmp"

    .line 65
    .local v0, "aKey":Ljava/lang/String;
    const/16 v12, 0x100

    new-array v6, v12, [I

    .line 66
    .local v6, "iS":[I
    const/16 v12, 0x100

    new-array v4, v12, [B

    .line 67
    .local v4, "iK":[B
    const/4 v1, 0x0

    .local v1, "i":I
    :goto_b
    const/16 v12, 0x100

    if-ge v1, v12, :cond_14

    .line 68
    aput v1, v6, v1

    .line 67
    add-int/lit8 v1, v1, 0x1

    goto :goto_b

    .line 69
    :cond_14
    const/4 v8, 0x1

    .line 70
    .local v8, "j":I
    const/4 v1, 0x0

    .local v1, "i":S
    :goto_16
    const/16 v12, 0x100

    if-ge v1, v12, :cond_4d

    .line 71
    sget-object v12, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v13, Ljava/lang/StringBuilder;

    invoke-direct {v13}, Ljava/lang/StringBuilder;-><init>()V

    const-string v14, "char:"

    invoke-virtual {v13, v14}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v13

    invoke-virtual {v0}, Ljava/lang/String;->length()I

    move-result v14

    rem-int v14, v1, v14

    invoke-virtual {v0, v14}, Ljava/lang/String;->charAt(I)C

    move-result v14

    invoke-virtual {v13, v14}, Ljava/lang/StringBuilder;->append(C)Ljava/lang/StringBuilder;

    move-result-object v13

    invoke-virtual {v13}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v13

    invoke-virtual {v12, v13}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 72
    invoke-virtual {v0}, Ljava/lang/String;->length()I

    move-result v12

    rem-int v12, v1, v12

    invoke-virtual {v0, v12}, Ljava/lang/String;->charAt(I)C

    move-result v12

    int-to-byte v12, v12

    aput-byte v12, v4, v1

    .line 70
    add-int/lit8 v12, v1, 0x1

    int-to-short v1, v12

    goto :goto_16

    .line 74
    :cond_4d
    const/4 v8, 0x0

    .line 75
    const/4 v1, 0x0

    .local v1, "i":I
    :goto_4f
    const/16 v12, 0xff

    if-ge v1, v12, :cond_66

    .line 76
    aget v12, v6, v1

    add-int/2addr v12, v8

    aget-byte v13, v4, v1

    add-int/2addr v12, v13

    rem-int/lit16 v8, v12, 0x100

    .line 77
    aget v10, v6, v1

    .line 78
    .local v10, "temp":I
    aget v12, v6, v8

    aput v12, v6, v1

    .line 79
    aput v10, v6, v8

    .line 75
    add-int/lit8 v1, v1, 0x1

    goto :goto_4f

    .line 81
    .end local v10    # "temp":I
    :cond_66
    const/4 v1, 0x0

    .line 82
    const/4 v8, 0x0

    .line 83
    invoke-virtual {p0}, Ljava/lang/String;->toCharArray()[C

    move-result-object v3

    .line 84
    .local v3, "iInputChar":[C
    array-length v12, v3

    new-array v5, v12, [C

    .line 85
    .local v5, "iOutputChar":[C
    const/4 v11, 0x0

    .local v11, "x":S
    :goto_70
    array-length v12, v3

    if-ge v11, v12, :cond_9a

    .line 86
    add-int/lit8 v12, v1, 0x1

    rem-int/lit16 v1, v12, 0x100

    .line 87
    aget v12, v6, v1

    add-int/2addr v12, v8

    rem-int/lit16 v8, v12, 0x100

    .line 88
    aget v10, v6, v1

    .line 89
    .restart local v10    # "temp":I
    aget v12, v6, v8

    aput v12, v6, v1

    .line 90
    aput v10, v6, v8

    .line 91
    aget v12, v6, v1

    aget v13, v6, v8

    rem-int/lit16 v13, v13, 0x100

    add-int/2addr v12, v13

    rem-int/lit16 v9, v12, 0x100

    .line 92
    .local v9, "t":I
    aget v7, v6, v9

    .line 93
    .local v7, "iY":I
    int-to-char v2, v7

    .line 94
    .local v2, "iCY":C
    aget-char v12, v3, v11

    xor-int/2addr v12, v2

    int-to-char v12, v12

    aput-char v12, v5, v11

    .line 85
    add-int/lit8 v12, v11, 0x1

    int-to-short v11, v12

    goto :goto_70

    .line 96
    .end local v2    # "iCY":C
    .end local v7    # "iY":I
    .end local v9    # "t":I
    .end local v10    # "temp":I
    :cond_9a
    new-instance v12, Ljava/lang/String;

    invoke-direct {v12, v5}, Ljava/lang/String;-><init>([C)V

    return-object v12
.end method

.method public static ttt()V
    .registers 2

    .prologue
    .line 100
    const v0, 0x3dcccccd    # 0.1f

    const v1, 0x3e4ccccd    # 0.2f

    invoke-static {v0, v1}, LInvoketTest;->ttttt(FF)V

    .line 101
    return-void
.end method

.method public static ttttt(FF)V
    .registers 5
    .param p0, "a"    # F
    .param p1, "b"    # F

    .prologue
    .line 103
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "v1 "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, p0}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, " v2 "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 104
    return-void
.end method


# virtual methods
.method public Test()V
    .registers 15
    .annotation runtime Lorg/junit/Test;
    .end annotation

    .prologue
    .line 26
    :try_start_0
    invoke-static {}, Larmadillo/Application;->InitDir()V

    .line 27
    const-class v5, LInvoketTest;

    invoke-virtual {v5}, Ljava/lang/Class;->getClassLoader()Ljava/lang/ClassLoader;

    move-result-object v5

    new-instance v12, Ljava/lang/StringBuilder;

    invoke-direct {v12}, Ljava/lang/StringBuilder;-><init>()V

    invoke-static {}, Larmadillo/Constant;->getProfile()Ljava/lang/String;

    move-result-object v13

    invoke-virtual {v12, v13}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v12

    const-string v13, "/log4j.properties"

    invoke-virtual {v12, v13}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v12

    invoke-virtual {v12}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v12

    invoke-virtual {v5, v12}, Ljava/lang/ClassLoader;->getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;

    move-result-object v5

    invoke-static {v5}, Lorg/apache/log4j/PropertyConfigurator;->configure(Ljava/io/InputStream;)V

    .line 28
    new-instance v2, Ljava/util/ArrayList;

    invoke-direct {v2}, Ljava/util/ArrayList;-><init>()V

    .line 29
    .local v2, "task":Ljava/util/List;, "Ljava/util/List<Larmadillo/result/TaskInfo;>;"
    const-string v4, "Test.zip"

    .line 30
    .local v4, "uuid":Ljava/lang/String;
    new-instance v3, Ljava/util/ArrayList;

    invoke-direct {v3}, Ljava/util/ArrayList;-><init>()V

    .line 31
    .local v3, "dexs":Ljava/util/List;, "Ljava/util/List<[B>;"
    new-instance v1, Ljava/util/zip/ZipFile;

    const-string v5, "C:\\Users\\Administrator\\Desktop\\base.apk"

    invoke-direct {v1, v5}, Ljava/util/zip/ZipFile;-><init>(Ljava/lang/String;)V

    .line 33
    .local v1, "zipFile":Ljava/util/zip/ZipFile;
    invoke-virtual {v1}, Ljava/util/zip/ZipFile;->entries()Ljava/util/Enumeration;

    move-result-object v7

    .line 34
    .local v7, "enumeration":Ljava/util/Enumeration;, "Ljava/util/Enumeration<+Ljava/util/zip/ZipEntry;>;"
    :cond_3e
    :goto_3e
    invoke-interface {v7}, Ljava/util/Enumeration;->hasMoreElements()Z

    move-result v5

    if-eqz v5, :cond_7c

    .line 35
    invoke-interface {v7}, Ljava/util/Enumeration;->nextElement()Ljava/lang/Object;

    move-result-object v11

    check-cast v11, Ljava/util/zip/ZipEntry;

    .line 36
    .local v11, "zipEntry":Ljava/util/zip/ZipEntry;
    invoke-virtual {v11}, Ljava/util/zip/ZipEntry;->getName()Ljava/lang/String;

    move-result-object v5

    const-string v12, "classes"

    invoke-virtual {v5, v12}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v5

    if-eqz v5, :cond_3e

    invoke-virtual {v11}, Ljava/util/zip/ZipEntry;->getName()Ljava/lang/String;

    move-result-object v5

    const-string v12, "dex"

    invoke-virtual {v5, v12}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z

    move-result v5

    if-eqz v5, :cond_3e

    .line 37
    new-instance v5, Ljava/util/zip/ZipEntry;

    invoke-virtual {v11}, Ljava/util/zip/ZipEntry;->getName()Ljava/lang/String;

    move-result-object v12

    invoke-direct {v5, v12}, Ljava/util/zip/ZipEntry;-><init>(Ljava/lang/String;)V

    invoke-virtual {v1, v5}, Ljava/util/zip/ZipFile;->getInputStream(Ljava/util/zip/ZipEntry;)Ljava/io/InputStream;

    move-result-object v5

    invoke-static {v5}, Larmadillo/utils/FileUtils;->toByte(Ljava/io/InputStream;)[B

    move-result-object v5

    invoke-interface {v3, v5}, Ljava/util/List;->add(Ljava/lang/Object;)Z
    :try_end_76
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_76} :catch_77
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_76} :catch_c5

    goto :goto_3e

    .line 51
    .end local v1    # "zipFile":Ljava/util/zip/ZipFile;
    .end local v2    # "task":Ljava/util/List;, "Ljava/util/List<Larmadillo/result/TaskInfo;>;"
    .end local v3    # "dexs":Ljava/util/List;, "Ljava/util/List<[B>;"
    .end local v4    # "uuid":Ljava/lang/String;
    .end local v7    # "enumeration":Ljava/util/Enumeration;, "Ljava/util/Enumeration<+Ljava/util/zip/ZipEntry;>;"
    .end local v11    # "zipEntry":Ljava/util/zip/ZipEntry;
    :catch_77
    move-exception v6

    .line 52
    .local v6, "e":Ljava/io/IOException;
    invoke-virtual {v6}, Ljava/io/IOException;->printStackTrace()V

    .line 56
    .end local v6    # "e":Ljava/io/IOException;
    :goto_7b
    return-void

    .line 39
    .restart local v1    # "zipFile":Ljava/util/zip/ZipFile;
    .restart local v2    # "task":Ljava/util/List;, "Ljava/util/List<Larmadillo/result/TaskInfo;>;"
    .restart local v3    # "dexs":Ljava/util/List;, "Ljava/util/List<[B>;"
    .restart local v4    # "uuid":Ljava/lang/String;
    .restart local v7    # "enumeration":Ljava/util/Enumeration;, "Ljava/util/Enumeration<+Ljava/util/zip/ZipEntry;>;"
    :cond_7c
    :try_start_7c
    new-instance v0, Larmadillo/Arm;

    sget-object v5, Larmadillo/enums/LanguageEnums;->DEFAULT:Larmadillo/enums/LanguageEnums;

    invoke-direct/range {v0 .. v5}, Larmadillo/Arm;-><init>(Ljava/util/zip/ZipFile;Ljava/util/List;Ljava/util/List;Ljava/lang/String;Larmadillo/enums/LanguageEnums;)V

    .line 40
    .local v0, "arm":Larmadillo/Arm;
    const-string v5, "{\"128\":[\"Lcom/ql/QL;\"],\"METHOD_SEPARATE\":[\"Lcom/Ks/MainActivity;\"]}"

    invoke-virtual {v0, v5}, Larmadillo/Arm;->setConfig(Ljava/lang/String;)V

    .line 42
    new-instance v10, Larmadillo/plugin/PluginClassloader;

    .line 43
    invoke-static {}, Ljava/lang/ClassLoader;->getSystemClassLoader()Ljava/lang/ClassLoader;

    move-result-object v5

    new-instance v12, Ljava/lang/StringBuilder;

    invoke-direct {v12}, Ljava/lang/StringBuilder;-><init>()V

    .line 44
    invoke-static {}, Larmadillo/Constant;->getPlugin()Ljava/io/File;

    move-result-object v13

    invoke-virtual {v13}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v13

    invoke-virtual {v12, v13}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v12

    sget-object v13, Ljava/io/File;->separator:Ljava/lang/String;

    invoke-virtual {v12, v13}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v12

    const-string v13, "VmpProtect-1.0-SNAPSHOT.jar"

    invoke-virtual {v12, v13}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v12

    invoke-virtual {v12}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v12

    invoke-direct {v10, v5, v12}, Larmadillo/plugin/PluginClassloader;-><init>(Ljava/lang/ClassLoader;Ljava/lang/String;)V

    .line 45
    .local v10, "pluginClassloader":Larmadillo/plugin/PluginClassloader;
    const-string v5, "armadillo.transformers.protece.vm.VmProtect"

    invoke-virtual {v10, v5}, Larmadillo/plugin/PluginClassloader;->loadClass(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v8

    .line 46
    .local v8, "loadClass":Ljava/lang/Class;, "Ljava/lang/Class<*>;"
    invoke-virtual {v8}, Ljava/lang/Class;->newInstance()Ljava/lang/Object;

    move-result-object v9

    .line 47
    .local v9, "newInstance":Ljava/lang/Object;
    check-cast v9, Larmadillo/transformers/base/BaseTransformer;

    .end local v9    # "newInstance":Ljava/lang/Object;
    invoke-virtual {v0, v9}, Larmadillo/Arm;->addTransformer(Larmadillo/transformers/base/BaseTransformer;)V

    .line 49
    invoke-virtual {v0}, Larmadillo/Arm;->Run()V
    :try_end_c4
    .catch Ljava/io/IOException; {:try_start_7c .. :try_end_c4} :catch_77
    .catch Ljava/lang/Exception; {:try_start_7c .. :try_end_c4} :catch_c5

    goto :goto_7b

    .line 53
    .end local v0    # "arm":Larmadillo/Arm;
    .end local v1    # "zipFile":Ljava/util/zip/ZipFile;
    .end local v2    # "task":Ljava/util/List;, "Ljava/util/List<Larmadillo/result/TaskInfo;>;"
    .end local v3    # "dexs":Ljava/util/List;, "Ljava/util/List<[B>;"
    .end local v4    # "uuid":Ljava/lang/String;
    .end local v7    # "enumeration":Ljava/util/Enumeration;, "Ljava/util/Enumeration<+Ljava/util/zip/ZipEntry;>;"
    .end local v8    # "loadClass":Ljava/lang/Class;, "Ljava/lang/Class<*>;"
    .end local v10    # "pluginClassloader":Larmadillo/plugin/PluginClassloader;
    :catch_c5
    move-exception v6

    .line 54
    .local v6, "e":Ljava/lang/Exception;
    invoke-virtual {v6}, Ljava/lang/Exception;->printStackTrace()V

    goto :goto_7b
.end method

.method public ss()V
    .registers 3
    .annotation runtime Lorg/junit/Test;
    .end annotation

    .prologue
    .line 60
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    const-string v1, "\u6d4b\u8bd5"

    invoke-static {v1}, LInvoketTest;->rc4(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 61
    return-void
.end method
