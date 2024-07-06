.class public Larmadillo/call/test$aaaa;
.super Larmadillo/call/test$bbbb;
.source "test.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Larmadillo/call/test;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x1
    name = "aaaa"
.end annotation


# instance fields
.field final synthetic this$0:Larmadillo/call/test;


# direct methods
.method public constructor <init>(Larmadillo/call/test;)V
    .registers 2
    .param p1, "this$0"    # Larmadillo/call/test;

    .prologue
    .line 33
    iput-object p1, p0, Larmadillo/call/test$aaaa;->this$0:Larmadillo/call/test;

    invoke-direct {p0, p1}, Larmadillo/call/test$bbbb;-><init>(Larmadillo/call/test;)V

    return-void
.end method

.method private outp(Ljava/lang/String;)V
    .registers 3
    .param p1, "string"    # Ljava/lang/String;

    .prologue
    .line 40
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    invoke-virtual {v0, p1}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 41
    return-void
.end method


# virtual methods
.method protected out(Ljava/lang/String;)V
    .registers 3
    .param p1, "string"    # Ljava/lang/String;

    .prologue
    .line 36
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    invoke-virtual {v0, p1}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 37
    return-void
.end method
