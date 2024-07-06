.class public Larmadillo/call/test$bbbb;
.super Ljava/lang/Object;
.source "test.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Larmadillo/call/test;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x1
    name = "bbbb"
.end annotation


# instance fields
.field final synthetic this$0:Larmadillo/call/test;


# direct methods
.method public constructor <init>(Larmadillo/call/test;)V
    .registers 2
    .param p1, "this$0"    # Larmadillo/call/test;

    .prologue
    .line 44
    iput-object p1, p0, Larmadillo/call/test$bbbb;->this$0:Larmadillo/call/test;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public String()Ljava/lang/String;
    .registers 3

    .prologue
    .line 50
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    const-string v1, "\u8c03\u7528\u6210\u529f"

    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 51
    const-string v0, "bbbb{}"

    return-object v0
.end method

.method public test(Ljava/lang/String;)V
    .registers 3
    .param p1, "string"    # Ljava/lang/String;

    .prologue
    .line 46
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    invoke-virtual {v0, p1}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 47
    return-void
.end method
