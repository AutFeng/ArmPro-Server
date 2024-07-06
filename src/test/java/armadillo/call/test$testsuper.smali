.class public Larmadillo/call/test$testsuper;
.super Larmadillo/call/test$aaaa;
.source "test.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Larmadillo/call/test;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x1
    name = "testsuper"
.end annotation


# instance fields
.field final synthetic this$0:Larmadillo/call/test;


# direct methods
.method public constructor <init>(Larmadillo/call/test;)V
    .registers 2
    .param p1, "this$0"    # Larmadillo/call/test;

    .prologue
    .line 29
    iput-object p1, p0, Larmadillo/call/test$testsuper;->this$0:Larmadillo/call/test;

    invoke-direct {p0, p1}, Larmadillo/call/test$aaaa;-><init>(Larmadillo/call/test;)V

    return-void
.end method
