.class public Ldecode;
.super Ljava/lang/Object;
.source "decode.java"


# instance fields
.field private rg_n33738:[I


# direct methods
.method public constructor <init>()V
    .registers 2

    .prologue
    .line 6
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 7
    const/16 v0, 0x8

    new-array v0, v0, [I

    fill-array-data v0, :array_e

    iput-object v0, p0, Ldecode;->rg_n33738:[I

    return-void

    nop

    :array_e
    .array-data 4
        0x1
        0x2
        0x3
        0x4
        0x5
        0x6
        0x7
        0x0
    .end array-data
.end method


# virtual methods
.method public ss()V
    .registers 3
    .annotation runtime Lorg/junit/Test;
    .end annotation

    .prologue
    .line 12
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    const-string v1, ""

    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 13
    return-void
.end method
