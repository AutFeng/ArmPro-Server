.class public Lmethodtest/monitor_enter;
.super Ljava/lang/Object;
.source "monitor_enter.java"


# direct methods
.method public constructor <init>()V
    .registers 1

    .prologue
    .line 3
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public ss()V
    .registers 3

    .prologue
    .line 5
    const-string v0, ""

    .line 6
    .local v0, "s":Ljava/lang/String;
    monitor-enter v0

    .line 7
    const/4 v1, 0x0

    :try_start_4
    invoke-static {v1}, Ljava/lang/System;->exit(I)V

    .line 8
    monitor-exit v0

    .line 9
    return-void

    .line 8
    :catchall_9
    move-exception v1

    monitor-exit v0
    :try_end_b
    .catchall {:try_start_4 .. :try_end_b} :catchall_9

    throw v1
.end method
