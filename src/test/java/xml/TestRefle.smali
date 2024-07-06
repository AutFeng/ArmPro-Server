.class public Lxml/TestRefle;
.super Lxml/testasb;
.source "TestRefle.java"


# direct methods
.method public constructor <init>()V
    .registers 1

    .prologue
    .line 5
    invoke-direct {p0}, Lxml/testasb;-><init>()V

    return-void
.end method


# virtual methods
.method public test()V
    .registers 1
    .annotation runtime Lorg/junit/Test;
    .end annotation

    .prologue
    .line 8
    invoke-static {p0}, Lxml/ssss;->get(Lxml/TestRefle;)V

    .line 9
    return-void
.end method
