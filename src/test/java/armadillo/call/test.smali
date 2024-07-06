.class public Larmadillo/call/test;
.super Ljava/lang/Object;
.source "test.java"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Larmadillo/call/test$bbbb;,
        Larmadillo/call/test$aaaa;,
        Larmadillo/call/test$testsuper;
    }
.end annotation


# static fields
.field private static strings:[Ljava/lang/String;


# direct methods
.method static constructor <clinit>()V
    .registers 3

    .prologue
    .line 15
    const/4 v0, 0x2

    new-array v0, v0, [Ljava/lang/String;

    sput-object v0, Larmadillo/call/test;->strings:[Ljava/lang/String;

    .line 16
    sget-object v0, Larmadillo/call/test;->strings:[Ljava/lang/String;

    const/4 v1, 0x0

    const-string v2, "armadillo.call.test$aaaa:out:(Ljava/lang/String;)V:super"

    aput-object v2, v0, v1

    .line 17
    return-void
.end method

.method public constructor <init>()V
    .registers 1

    .prologue
    .line 10
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static aaaa()V
    .registers 1
    .annotation runtime Lorg/junit/Test;
    .end annotation

    .prologue
    .line 121
    const-class v0, Larmadillo/call/test;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    .line 130
    return-void
.end method

.method public static test(ZBSCIJFDLarmadillo/call/test;IIIIIIIIZ)V
    .registers 50
    .param p0, "a"    # Z
    .param p1, "b"    # B
    .param p2, "c"    # S
    .param p3, "d"    # C
    .param p4, "e"    # I
    .param p5, "f"    # J
    .param p7, "g"    # F
    .param p8, "h"    # D
    .param p10, "test"    # Larmadillo/call/test;
    .param p11, "aaa"    # I
    .param p12, "bbb"    # I
    .param p13, "ccc"    # I
    .param p14, "ddd"    # I
    .param p15, "eee"    # I
    .param p16, "fff"    # I
    .param p17, "hhh"    # I
    .param p18, "iii"    # I
    .param p19, "cxcx"    # Z

    .prologue
    .line 73
    move/from16 v2, p0

    .line 74
    .local v2, "aa":Z
    move/from16 v4, p1

    .line 75
    .local v4, "bb":B
    move/from16 v6, p2

    .line 76
    .local v6, "cc":S
    move/from16 v8, p3

    .line 77
    .local v8, "dd":C
    move/from16 v10, p4

    .line 78
    .local v10, "ee":I
    move-wide/from16 v12, p5

    .line 79
    .local v12, "ff":J
    move/from16 v15, p7

    .line 80
    .local v15, "gg":F
    move-wide/from16 v16, p8

    .line 81
    .local v16, "hh":D
    move-wide/from16 v24, v16

    .line 82
    .local v24, "ssss":D
    move-object/from16 v26, p10

    .line 83
    .local v26, "ssssss":Larmadillo/call/test;
    move/from16 v3, p11

    .line 84
    .local v3, "aaaa":I
    move/from16 v5, p12

    .line 85
    .local v5, "bbbb":I
    move/from16 v7, p13

    .line 86
    .local v7, "cccc":I
    move/from16 v9, p14

    .line 87
    .local v9, "dddd":I
    move/from16 v11, p15

    .line 88
    .local v11, "eeee":I
    move/from16 v14, p16

    .line 89
    .local v14, "ffff":I
    move/from16 v18, p17

    .line 90
    .local v18, "hhhh":I
    move/from16 v19, p18

    .line 91
    .local v19, "iiii":I
    move/from16 v27, p19

    .line 92
    .local v27, "ttttt":Z
    move-object/from16 v21, v26

    .line 93
    .local v21, "object":Larmadillo/call/test;
    const/16 v28, 0x64

    .line 94
    .local v28, "zzz":I
    invoke-static/range {v28 .. v28}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v22

    .line 95
    .local v22, "object1":Ljava/lang/Integer;
    const/16 v20, 0x1

    .line 96
    .local v20, "kkkk":Z
    invoke-static/range {v20 .. v20}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;

    move-result-object v23

    .line 97
    .local v23, "object2":Ljava/lang/Boolean;
    sget-object v29, Ljava/lang/System;->out:Ljava/io/PrintStream;

    move-object/from16 v0, v29

    move/from16 v1, v27

    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(Z)V

    .line 98
    return-void
.end method


# virtual methods
.method public aaa()V
    .registers 4
    .annotation runtime Lorg/junit/Test;
    .end annotation

    .prologue
    .line 102
    const-string v1, "   "

    invoke-virtual {v1}, Ljava/lang/String;->length()I

    move-result v1

    const/4 v2, 0x1

    if-eq v1, v2, :cond_12

    .line 103
    const v0, 0x783c9f15

    .line 104
    .local v0, "sss":I
    sget-object v1, Ljava/lang/System;->out:Ljava/io/PrintStream;

    invoke-virtual {v1, v0}, Ljava/io/PrintStream;->println(I)V

    .line 117
    .end local v0    # "sss":I
    :goto_11
    return-void

    .line 106
    :cond_12
    sget-object v1, Ljava/lang/System;->out:Ljava/io/PrintStream;

    invoke-virtual {v1}, Ljava/io/PrintStream;->println()V

    goto :goto_11
.end method

.method public test()V
    .registers 7
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/lang/Throwable;
        }
    .end annotation

    .annotation runtime Lorg/junit/Test;
    .end annotation

    .prologue
    .line 21
    new-instance v2, Larmadillo/call/test$testsuper;

    invoke-direct {v2, p0}, Larmadillo/call/test$testsuper;-><init>(Larmadillo/call/test;)V

    .line 22
    .local v2, "testsuper":Larmadillo/call/test$testsuper;
    invoke-virtual {v2}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    move-result-object v3

    const-string v4, "String"

    const/4 v5, 0x0

    invoke-virtual {v3, v4, v5}, Ljava/lang/Class;->getMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;

    move-result-object v1

    .line 23
    .local v1, "method":Ljava/lang/reflect/Method;
    const/4 v3, 0x0

    new-array v3, v3, [Ljava/lang/Object;

    invoke-virtual {v1, v2, v3}, Ljava/lang/reflect/Method;->invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;

    .line 26
    invoke-static {}, Ljava/lang/invoke/MethodHandles;->lookup()Ljava/lang/invoke/MethodHandles$Lookup;

    move-result-object v0

    .line 27
    .local v0, "lookup":Ljava/lang/invoke/MethodHandles$Lookup;
    return-void
.end method
