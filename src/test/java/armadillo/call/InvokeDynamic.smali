.class public Larmadillo/call/InvokeDynamic;
.super Ljava/lang/Object;
.source "InvokeDynamic.java"


# direct methods
.method public constructor <init>()V
    .registers 1

    .prologue
    .line 9
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static callSite(I[Ljava/lang/String;Ljava/lang/invoke/MethodHandles$Lookup;)Ljava/lang/invoke/CallSite;
    .registers 18
    .param p0, "index"    # I
    .param p1, "strings"    # [Ljava/lang/String;
    .param p2, "lookup"    # Ljava/lang/invoke/MethodHandles$Lookup;

    .prologue
    .line 11
    const/4 v2, 0x0

    .line 13
    .local v2, "callSite":Ljava/lang/invoke/CallSite;
    :try_start_1
    aget-object v10, p1, p0

    const-string v11, ":"

    invoke-virtual {v10, v11}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v9

    .line 14
    .local v9, "var":[Ljava/lang/String;
    const/4 v10, 0x2

    aget-object v10, v9, v10

    const-class v11, Larmadillo/call/InvokeDynamic;

    invoke-virtual {v11}, Ljava/lang/Class;->getClassLoader()Ljava/lang/ClassLoader;

    move-result-object v11

    invoke-static {v10, v11}, Ljava/lang/invoke/MethodType;->fromMethodDescriptorString(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;

    move-result-object v8

    .line 15
    .local v8, "type":Ljava/lang/invoke/MethodType;
    const/4 v10, 0x3

    aget-object v1, v9, v10

    .line 16
    .local v1, "action":Ljava/lang/String;
    const-string v10, "virtual"

    invoke-virtual {v10, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    :try_end_1e
    .catch Ljava/lang/Throwable; {:try_start_1 .. :try_end_1e} :catch_1d9

    move-result v10

    if-eqz v10, :cond_99

    .line 18
    :try_start_21
    new-instance v3, Ljava/lang/invoke/ConstantCallSite;

    const/4 v10, 0x0

    aget-object v10, v9, v10

    .line 19
    invoke-static {v10}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v10

    const/4 v11, 0x1

    aget-object v11, v9, v11

    move-object/from16 v0, p2

    invoke-virtual {v0, v10, v11, v8}, Ljava/lang/invoke/MethodHandles$Lookup;->findVirtual(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;

    move-result-object v10

    invoke-direct {v3, v10}, Ljava/lang/invoke/ConstantCallSite;-><init>(Ljava/lang/invoke/MethodHandle;)V
    :try_end_36
    .catch Ljava/lang/Exception; {:try_start_21 .. :try_end_36} :catch_38
    .catch Ljava/lang/Throwable; {:try_start_21 .. :try_end_36} :catch_1d9

    .end local v2    # "callSite":Ljava/lang/invoke/CallSite;
    .local v3, "callSite":Ljava/lang/invoke/CallSite;
    move-object v2, v3

    .line 90
    .end local v1    # "action":Ljava/lang/String;
    .end local v3    # "callSite":Ljava/lang/invoke/CallSite;
    .end local v8    # "type":Ljava/lang/invoke/MethodType;
    .end local v9    # "var":[Ljava/lang/String;
    .restart local v2    # "callSite":Ljava/lang/invoke/CallSite;
    :cond_37
    :goto_37
    return-object v2

    .line 20
    .restart local v1    # "action":Ljava/lang/String;
    .restart local v8    # "type":Ljava/lang/invoke/MethodType;
    .restart local v9    # "var":[Ljava/lang/String;
    :catch_38
    move-exception v5

    .line 21
    .local v5, "e":Ljava/lang/Exception;
    :try_start_39
    invoke-virtual {v5}, Ljava/lang/Exception;->printStackTrace()V

    .line 22
    const/4 v4, 0x0

    .line 23
    .local v4, "clz":Ljava/lang/Class;, "Ljava/lang/Class<*>;"
    const/4 v10, 0x0

    aget-object v10, v9, v10

    invoke-static {v10}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/Class;->getMethods()[Ljava/lang/reflect/Method;

    move-result-object v11

    array-length v12, v11

    const/4 v10, 0x0

    :goto_4a
    if-ge v10, v12, :cond_5f

    aget-object v6, v11, v10

    .line 24
    .local v6, "method":Ljava/lang/reflect/Method;
    invoke-virtual {v6}, Ljava/lang/reflect/Method;->getName()Ljava/lang/String;

    move-result-object v13

    const/4 v14, 0x1

    aget-object v14, v9, v14

    invoke-virtual {v13, v14}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v13

    if-eqz v13, :cond_93

    .line 25
    invoke-virtual {v6}, Ljava/lang/reflect/Method;->getDeclaringClass()Ljava/lang/Class;

    move-result-object v4

    .line 29
    .end local v6    # "method":Ljava/lang/reflect/Method;
    :cond_5f
    if-nez v4, :cond_83

    .line 30
    const/4 v10, 0x0

    aget-object v10, v9, v10

    invoke-static {v10}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/Class;->getDeclaredMethods()[Ljava/lang/reflect/Method;

    move-result-object v11

    array-length v12, v11

    const/4 v10, 0x0

    :goto_6e
    if-ge v10, v12, :cond_83

    aget-object v6, v11, v10

    .line 31
    .restart local v6    # "method":Ljava/lang/reflect/Method;
    invoke-virtual {v6}, Ljava/lang/reflect/Method;->getName()Ljava/lang/String;

    move-result-object v13

    const/4 v14, 0x1

    aget-object v14, v9, v14

    invoke-virtual {v13, v14}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v13

    if-eqz v13, :cond_96

    .line 32
    invoke-virtual {v6}, Ljava/lang/reflect/Method;->getDeclaringClass()Ljava/lang/Class;

    move-result-object v4

    .line 37
    .end local v6    # "method":Ljava/lang/reflect/Method;
    :cond_83
    new-instance v3, Ljava/lang/invoke/ConstantCallSite;

    const/4 v10, 0x1

    aget-object v10, v9, v10

    .line 38
    move-object/from16 v0, p2

    invoke-virtual {v0, v4, v10, v8}, Ljava/lang/invoke/MethodHandles$Lookup;->findVirtual(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;

    move-result-object v10

    invoke-direct {v3, v10}, Ljava/lang/invoke/ConstantCallSite;-><init>(Ljava/lang/invoke/MethodHandle;)V

    .end local v2    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v3    # "callSite":Ljava/lang/invoke/CallSite;
    move-object v2, v3

    .line 39
    .end local v3    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v2    # "callSite":Ljava/lang/invoke/CallSite;
    goto :goto_37

    .line 23
    .restart local v6    # "method":Ljava/lang/reflect/Method;
    :cond_93
    add-int/lit8 v10, v10, 0x1

    goto :goto_4a

    .line 30
    :cond_96
    add-int/lit8 v10, v10, 0x1

    goto :goto_6e

    .line 40
    .end local v4    # "clz":Ljava/lang/Class;, "Ljava/lang/Class<*>;"
    .end local v5    # "e":Ljava/lang/Exception;
    .end local v6    # "method":Ljava/lang/reflect/Method;
    :cond_99
    const-string v10, "static"

    invoke-virtual {v10, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    :try_end_9e
    .catch Ljava/lang/Throwable; {:try_start_39 .. :try_end_9e} :catch_1d9

    move-result v10

    if-eqz v10, :cond_11a

    .line 42
    :try_start_a1
    new-instance v3, Ljava/lang/invoke/ConstantCallSite;

    const/4 v10, 0x0

    aget-object v10, v9, v10

    .line 43
    invoke-static {v10}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v10

    const/4 v11, 0x1

    aget-object v11, v9, v11

    move-object/from16 v0, p2

    invoke-virtual {v0, v10, v11, v8}, Ljava/lang/invoke/MethodHandles$Lookup;->findStatic(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;

    move-result-object v10

    invoke-direct {v3, v10}, Ljava/lang/invoke/ConstantCallSite;-><init>(Ljava/lang/invoke/MethodHandle;)V
    :try_end_b6
    .catch Ljava/lang/Exception; {:try_start_a1 .. :try_end_b6} :catch_b8
    .catch Ljava/lang/Throwable; {:try_start_a1 .. :try_end_b6} :catch_1d9

    .end local v2    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v3    # "callSite":Ljava/lang/invoke/CallSite;
    move-object v2, v3

    .line 63
    .end local v3    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v2    # "callSite":Ljava/lang/invoke/CallSite;
    goto :goto_37

    .line 44
    :catch_b8
    move-exception v5

    .line 45
    .restart local v5    # "e":Ljava/lang/Exception;
    :try_start_b9
    invoke-virtual {v5}, Ljava/lang/Exception;->printStackTrace()V

    .line 46
    const/4 v4, 0x0

    .line 47
    .restart local v4    # "clz":Ljava/lang/Class;, "Ljava/lang/Class<*>;"
    const/4 v10, 0x0

    aget-object v10, v9, v10

    invoke-static {v10}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/Class;->getMethods()[Ljava/lang/reflect/Method;

    move-result-object v11

    array-length v12, v11

    const/4 v10, 0x0

    :goto_ca
    if-ge v10, v12, :cond_df

    aget-object v6, v11, v10

    .line 48
    .restart local v6    # "method":Ljava/lang/reflect/Method;
    invoke-virtual {v6}, Ljava/lang/reflect/Method;->getName()Ljava/lang/String;

    move-result-object v13

    const/4 v14, 0x1

    aget-object v14, v9, v14

    invoke-virtual {v13, v14}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v13

    if-eqz v13, :cond_114

    .line 49
    invoke-virtual {v6}, Ljava/lang/reflect/Method;->getDeclaringClass()Ljava/lang/Class;

    move-result-object v4

    .line 53
    .end local v6    # "method":Ljava/lang/reflect/Method;
    :cond_df
    if-nez v4, :cond_103

    .line 54
    const/4 v10, 0x0

    aget-object v10, v9, v10

    invoke-static {v10}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/Class;->getDeclaredMethods()[Ljava/lang/reflect/Method;

    move-result-object v11

    array-length v12, v11

    const/4 v10, 0x0

    :goto_ee
    if-ge v10, v12, :cond_103

    aget-object v6, v11, v10

    .line 55
    .restart local v6    # "method":Ljava/lang/reflect/Method;
    invoke-virtual {v6}, Ljava/lang/reflect/Method;->getName()Ljava/lang/String;

    move-result-object v13

    const/4 v14, 0x1

    aget-object v14, v9, v14

    invoke-virtual {v13, v14}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v13

    if-eqz v13, :cond_117

    .line 56
    invoke-virtual {v6}, Ljava/lang/reflect/Method;->getDeclaringClass()Ljava/lang/Class;

    move-result-object v4

    .line 61
    .end local v6    # "method":Ljava/lang/reflect/Method;
    :cond_103
    new-instance v3, Ljava/lang/invoke/ConstantCallSite;

    const/4 v10, 0x1

    aget-object v10, v9, v10

    .line 62
    move-object/from16 v0, p2

    invoke-virtual {v0, v4, v10, v8}, Ljava/lang/invoke/MethodHandles$Lookup;->findStatic(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;

    move-result-object v10

    invoke-direct {v3, v10}, Ljava/lang/invoke/ConstantCallSite;-><init>(Ljava/lang/invoke/MethodHandle;)V

    .end local v2    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v3    # "callSite":Ljava/lang/invoke/CallSite;
    move-object v2, v3

    .line 63
    .end local v3    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v2    # "callSite":Ljava/lang/invoke/CallSite;
    goto/16 :goto_37

    .line 47
    .restart local v6    # "method":Ljava/lang/reflect/Method;
    :cond_114
    add-int/lit8 v10, v10, 0x1

    goto :goto_ca

    .line 54
    :cond_117
    add-int/lit8 v10, v10, 0x1

    goto :goto_ee

    .line 64
    .end local v4    # "clz":Ljava/lang/Class;, "Ljava/lang/Class<*>;"
    .end local v5    # "e":Ljava/lang/Exception;
    .end local v6    # "method":Ljava/lang/reflect/Method;
    :cond_11a
    const-string v10, "super"

    invoke-virtual {v10, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v10

    if-eqz v10, :cond_13a

    .line 65
    new-instance v3, Ljava/lang/invoke/ConstantCallSite;

    const/4 v10, 0x0

    aget-object v10, v9, v10

    .line 66
    invoke-static {v10}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v10

    const/4 v11, 0x1

    aget-object v11, v9, v11

    move-object/from16 v0, p2

    invoke-virtual {v0, v10, v11, v8}, Ljava/lang/invoke/MethodHandles$Lookup;->findVirtual(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;

    move-result-object v10

    invoke-direct {v3, v10}, Ljava/lang/invoke/ConstantCallSite;-><init>(Ljava/lang/invoke/MethodHandle;)V

    .end local v2    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v3    # "callSite":Ljava/lang/invoke/CallSite;
    move-object v2, v3

    .end local v3    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v2    # "callSite":Ljava/lang/invoke/CallSite;
    goto/16 :goto_37

    .line 67
    :cond_13a
    const-string v10, "constructor"

    invoke-virtual {v10, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v10

    if-eqz v10, :cond_157

    .line 68
    new-instance v3, Ljava/lang/invoke/ConstantCallSite;

    const/4 v10, 0x0

    aget-object v10, v9, v10

    .line 69
    invoke-static {v10}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v10

    move-object/from16 v0, p2

    invoke-virtual {v0, v10, v8}, Ljava/lang/invoke/MethodHandles$Lookup;->findConstructor(Ljava/lang/Class;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;

    move-result-object v10

    invoke-direct {v3, v10}, Ljava/lang/invoke/ConstantCallSite;-><init>(Ljava/lang/invoke/MethodHandle;)V

    .end local v2    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v3    # "callSite":Ljava/lang/invoke/CallSite;
    move-object v2, v3

    .end local v3    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v2    # "callSite":Ljava/lang/invoke/CallSite;
    goto/16 :goto_37

    .line 70
    :cond_157
    const-string v10, "get"

    invoke-virtual {v10, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    :try_end_15c
    .catch Ljava/lang/Throwable; {:try_start_b9 .. :try_end_15c} :catch_1d9

    move-result v10

    if-eqz v10, :cond_198

    .line 72
    :try_start_15f
    new-instance v3, Ljava/lang/invoke/ConstantCallSite;

    const/4 v10, 0x0

    aget-object v10, v9, v10

    .line 73
    invoke-static {v10}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v10

    const/4 v11, 0x1

    aget-object v11, v9, v11

    invoke-virtual {v8}, Ljava/lang/invoke/MethodType;->returnType()Ljava/lang/Class;

    move-result-object v12

    move-object/from16 v0, p2

    invoke-virtual {v0, v10, v11, v12}, Ljava/lang/invoke/MethodHandles$Lookup;->findGetter(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;

    move-result-object v10

    invoke-direct {v3, v10}, Ljava/lang/invoke/ConstantCallSite;-><init>(Ljava/lang/invoke/MethodHandle;)V
    :try_end_178
    .catch Ljava/lang/Exception; {:try_start_15f .. :try_end_178} :catch_17b
    .catch Ljava/lang/Throwable; {:try_start_15f .. :try_end_178} :catch_1d9

    .end local v2    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v3    # "callSite":Ljava/lang/invoke/CallSite;
    move-object v2, v3

    .line 77
    .end local v3    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v2    # "callSite":Ljava/lang/invoke/CallSite;
    goto/16 :goto_37

    .line 74
    :catch_17b
    move-exception v5

    .line 75
    .restart local v5    # "e":Ljava/lang/Exception;
    :try_start_17c
    new-instance v3, Ljava/lang/invoke/ConstantCallSite;

    const/4 v10, 0x0

    aget-object v10, v9, v10

    .line 76
    invoke-static {v10}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v10

    const/4 v11, 0x1

    aget-object v11, v9, v11

    invoke-virtual {v8}, Ljava/lang/invoke/MethodType;->returnType()Ljava/lang/Class;

    move-result-object v12

    move-object/from16 v0, p2

    invoke-virtual {v0, v10, v11, v12}, Ljava/lang/invoke/MethodHandles$Lookup;->findStaticGetter(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;

    move-result-object v10

    invoke-direct {v3, v10}, Ljava/lang/invoke/ConstantCallSite;-><init>(Ljava/lang/invoke/MethodHandle;)V

    .end local v2    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v3    # "callSite":Ljava/lang/invoke/CallSite;
    move-object v2, v3

    .line 77
    .end local v3    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v2    # "callSite":Ljava/lang/invoke/CallSite;
    goto/16 :goto_37

    .line 78
    .end local v5    # "e":Ljava/lang/Exception;
    :cond_198
    const-string v10, "set"

    invoke-virtual {v10, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    :try_end_19d
    .catch Ljava/lang/Throwable; {:try_start_17c .. :try_end_19d} :catch_1d9

    move-result v10

    if-eqz v10, :cond_37

    .line 80
    :try_start_1a0
    new-instance v3, Ljava/lang/invoke/ConstantCallSite;

    const/4 v10, 0x0

    aget-object v10, v9, v10

    .line 81
    invoke-static {v10}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v10

    const/4 v11, 0x1

    aget-object v11, v9, v11

    invoke-virtual {v8}, Ljava/lang/invoke/MethodType;->returnType()Ljava/lang/Class;

    move-result-object v12

    move-object/from16 v0, p2

    invoke-virtual {v0, v10, v11, v12}, Ljava/lang/invoke/MethodHandles$Lookup;->findSetter(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;

    move-result-object v10

    invoke-direct {v3, v10}, Ljava/lang/invoke/ConstantCallSite;-><init>(Ljava/lang/invoke/MethodHandle;)V
    :try_end_1b9
    .catch Ljava/lang/Exception; {:try_start_1a0 .. :try_end_1b9} :catch_1bc
    .catch Ljava/lang/Throwable; {:try_start_1a0 .. :try_end_1b9} :catch_1d9

    .end local v2    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v3    # "callSite":Ljava/lang/invoke/CallSite;
    move-object v2, v3

    .line 85
    .end local v3    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v2    # "callSite":Ljava/lang/invoke/CallSite;
    goto/16 :goto_37

    .line 82
    :catch_1bc
    move-exception v5

    .line 83
    .restart local v5    # "e":Ljava/lang/Exception;
    :try_start_1bd
    new-instance v3, Ljava/lang/invoke/ConstantCallSite;

    const/4 v10, 0x0

    aget-object v10, v9, v10

    .line 84
    invoke-static {v10}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v10

    const/4 v11, 0x1

    aget-object v11, v9, v11

    invoke-virtual {v8}, Ljava/lang/invoke/MethodType;->returnType()Ljava/lang/Class;

    move-result-object v12

    move-object/from16 v0, p2

    invoke-virtual {v0, v10, v11, v12}, Ljava/lang/invoke/MethodHandles$Lookup;->findStaticSetter(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;

    move-result-object v10

    invoke-direct {v3, v10}, Ljava/lang/invoke/ConstantCallSite;-><init>(Ljava/lang/invoke/MethodHandle;)V
    :try_end_1d6
    .catch Ljava/lang/Throwable; {:try_start_1bd .. :try_end_1d6} :catch_1d9

    .end local v2    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v3    # "callSite":Ljava/lang/invoke/CallSite;
    move-object v2, v3

    .end local v3    # "callSite":Ljava/lang/invoke/CallSite;
    .restart local v2    # "callSite":Ljava/lang/invoke/CallSite;
    goto/16 :goto_37

    .line 87
    .end local v1    # "action":Ljava/lang/String;
    .end local v5    # "e":Ljava/lang/Exception;
    .end local v8    # "type":Ljava/lang/invoke/MethodType;
    .end local v9    # "var":[Ljava/lang/String;
    :catch_1d9
    move-exception v7

    .line 88
    .local v7, "throwable":Ljava/lang/Throwable;
    invoke-virtual {v7}, Ljava/lang/Throwable;->printStackTrace()V

    goto/16 :goto_37
.end method
