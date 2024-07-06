package tree;

import action.ActionEnumsTest;
import armadillo.enums.LanguageEnums;
import armadillo.model.tree.ConfigRule;
import armadillo.model.tree.Node;
import armadillo.model.tree.ResourceRule;
import armadillo.model.tree.TreeNode;
import armadillo.utils.ActionUtils;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Test {
    @org.junit.Test
    public void t() {
        List<TreeNode> root_tree = new ArrayList<>();


        TreeNode arm = new TreeNode("arm服务", true);
        {
            arm.setChild(Lists.newArrayList(
                    new Node(
                            "远程注册机",
                            "可远程控制应用",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            1,
                            0x8000000,
                            Lists.newArrayList(
                                    new ResourceRule("AndroidManifest.xml"),
                                    new ResourceRule("classes", "dex"),
                                    new ResourceRule("resources.arsc")),
                            Lists.newArrayList(
                                    new Node("全局模式1", "兼容性强/不改变任何原程序代码流程", null, 0, 0),
                                    new Node("全局模式2", "基于替换Application实现", null, 0, 3),
                                    new Node("单例模式", "需指定对应入口函数", null, 0, 1, false, true),
                                    new Node("页面模式", "需指定对应入口", null, 0, 2, false, true)
                            )),
                    new Node(
                            "Nop抽取壳Pro",
                            "可脱函数抽取壳",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            8,
                            0x1),
                    new Node(
                            "一键脱壳Pro",
                            "可脱部分函数抽取壳",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            3,
                            0x1),
                    new Node(
                            "一键脱壳",
                            "基于Xposed脱壳",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            3,
                            0x200000000L),
                    new Node(
                            "去除签名校验兼容版",
                            "可移除老版本加固的签名",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            8,
                            0x10000000,
                            Collections.emptyList(),
                            Lists.newArrayList(
                                    new Node("原包模式", "体积大/不支持多dex", null, 0, 0, Lists.newArrayList(
                                            new ResourceRule("AndroidManifest.xml"),
                                            new ResourceRule("classes", "dex"))),
                                    new Node("精简模式", "体积小/不支持多dex", null, 0, 1, Lists.newArrayList(
                                            new ResourceRule("AndroidManifest.xml"),
                                            new ResourceRule("META-INF", null),
                                            new ResourceRule("classes", "dex"),
                                            new ResourceRule("resources.arsc")))
                            )),
                    new Node(
                            "去除签名校验Pro",
                            "可移除新版本加固的签名",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            8,
                            0x2000000,
                            Collections.emptyList(),
                            Lists.newArrayList(
                                    new Node("原包模式", "体积大", null, 0, 0, Lists.newArrayList(
                                            new ResourceRule("AndroidManifest.xml"),
                                            new ResourceRule("classes", "dex"))),
                                    new Node("精简模式", "体积小", null, 0, 1, Lists.newArrayList(
                                            new ResourceRule("AndroidManifest.xml"),
                                            new ResourceRule("META-INF", null),
                                            new ResourceRule("classes", "dex"),
                                            new ResourceRule("resources.arsc")))
                            )),
                    new Node(
                            "Apk一代加固",
                            "保护Dex文件",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            1,
                            0x400000,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex"),
                                    new ResourceRule("AndroidManifest.xml"))),
                    new Node(
                            "Dex2C",
                            "保护函数为本机代码",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            4,
                            0x2000,
                            Lists.newArrayList(new ResourceRule("classes.dex")),
                            true,
                            Lists.newArrayList(new ResourceRule("classes.dex"))),
                    new Node(
                            "DexSE",
                            "保护调用代码为本机代码",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            9,
                            0x20000000,
                            Lists.newArrayList(new ResourceRule("classes", "dex")),
                            true,
                            Lists.newArrayList(new ResourceRule("classes", "dex")))
            ));
        }

        TreeNode soft = new TreeNode("应用安全", false);
        {
            soft.setChild(Lists.newArrayList(
                    new Node(
                            "Vpn检测",
                            "检测是否开启VPN代理",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            0,
                            0x100,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex"),
                                    new ResourceRule("AndroidManifest.xml"))),
                    new Node(
                            "Xposed检测",
                            "检测是否存在Xposed环境",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            0,
                            0x200,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex"),
                                    new ResourceRule("AndroidManifest.xml"))),
                    new Node(
                            "模拟器检测",
                            "检测是否运行在模拟器环境",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            0,
                            0x200000,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex"),
                                    new ResourceRule("AndroidManifest.xml"))),
                    new Node(
                            "防截图",
                            "防止软件被系统截图",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            0,
                            0x80000000L,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex"),
                                    new ResourceRule("AndroidManifest.xml"))),
                    new Node(
                            "断网检测",
                            "无网络就退出",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            0,
                            0x100000000L,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex"),
                                    new ResourceRule("AndroidManifest.xml"))),
                    new Node(
                            "去除版本号检测",
                            "可移除部分更新检测",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            0,
                            0x400,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex"),
                                    new ResourceRule("AndroidManifest.xml"))),
                    new Node(
                            "去除普通签名验证",
                            "可移除部分签名验证",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            0,
                            0x1000,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex"),
                                    new ResourceRule("META-INF", null),
                                    new ResourceRule("AndroidManifest.xml"))),
                    new Node(
                            "去除应用安装检测",
                            "可移除部分应用安装检测",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            0,
                            0x800,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex"),
                                    new ResourceRule("AndroidManifest.xml"))),
                    new Node(
                            "强制弹窗可取消",
                            "可强制更改不可取消的弹窗为可取消",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            0,
                            0x1000000,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex"),
                                    new ResourceRule("AndroidManifest.xml"))),
                    new Node(
                            "移除VPN代理检测",
                            "可移除部分VPN代理检测",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            0,
                            0x4000000,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex"),
                                    new ResourceRule("AndroidManifest.xml"))),
                    new Node(
                            "移除Dex调试信息",
                            "移除Dex所有Debug调试信息",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            0,
                            0x10000,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex"))),
                    new Node(
                            "Dex插入无用调试信息",
                            "Dex插入大量无用Debug调试信息",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            0,
                            0x20000,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex"))),
                    new Node(
                            "字符串加密",
                            "可移除部分VPN代理检测",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            1,
                            0x8,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex"))),
                    new Node(
                            "资源ID加密",
                            "可移除部分VPN代理检测",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            1,
                            0x20,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex"))),
                    new Node(
                            "布局访问加密",
                            "保护布局文件不被篡改",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            2,
                            0x4,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex"),
                                    new ResourceRule("resources.arsc"),
                                    new ResourceRule(null, "xml"))),
                    new Node(
                            "Assets资产文件访问加密",
                            "保护Assets资产文件不被篡改",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            2,
                            0x2,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex"),
                                    new ResourceRule("assets", null))),
                    new Node(
                            "Assets资产全加密",
                            "保护Assets资产文件不被篡改",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            3,
                            0x40,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex"),
                                    new ResourceRule("assets", null))),
                    new Node(
                            "全资产加密",
                            "保护Assets资产文件不被篡改",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            4,
                            0x8000,
                            Lists.newArrayList(
                                    new ResourceRule("!lib", null),
                                    new ResourceRule("!classes", "!dex"),
                                    new ResourceRule("!META-INF", null))),
                    new Node(
                            "普通签名验证",
                            "保护APK不被二次签名篡改",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            1,
                            0x80000,
                            Lists.newArrayList(
                                    new ResourceRule("AndroidManifest.xml"),
                                    new ResourceRule("classes", "dex"),
                                    new ResourceRule("META-INF", null))),
                    new Node(
                            "高级签名验证",
                            "保护APK不被二次签名篡改",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            3,
                            0x100000,
                            Lists.newArrayList(
                                    new ResourceRule("AndroidManifest.xml"),
                                    new ResourceRule("classes", "dex"),
                                    new ResourceRule("META-INF", null))),
                    new Node(
                            "函数调用加密",
                            "隐藏真实Dex函数调用",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            3,
                            0x10,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex")),
                            true,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex"))),
                    new Node(
                            "虚假控制流",
                            "保护Dex方法不被反编译为可阅读的伪java代码",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            3,
                            0x80,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex")),
                            true,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex"))),
                    new Node(
                            "函数调用分离",
                            "混淆真实Dex函数调用",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            5,
                            0x40000,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex")),
                            true,
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex")))
            ));
        }

        TreeNode ad = new TreeNode("移动广告服务", true);
        {
            ad.setChild(Lists.newArrayList(
                    new Node(
                            "Admob",
                            "Admob移动广告",
                            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png",
                            0,
                            0x100,
                            Lists.newArrayList(
                                    new ConfigRule("", "应用KEY", "key")),
                            Lists.newArrayList(
                                    new ResourceRule("classes", "dex"),
                                    new ResourceRule("AndroidManifest.xml")),
                            false,
                            false,
                            null,
                            null)
            ));
        }

        root_tree.add(arm);
        root_tree.add(soft);
        root_tree.add(ad);
        System.out.println(new Gson().toJson(root_tree));
    }

    @org.junit.Test
    public void s() throws IOException {
        long type = 0;
        List<ActionEnumsTest> actionEnumsTests = new ArrayList<>();
        actionEnumsTests.add(ActionEnumsTest.SHELL);
        actionEnumsTests.add(ActionEnumsTest.HOOK_SIGN);
        for (ActionEnumsTest enumsTest : actionEnumsTests) {
            type = type | enumsTest.getType();
        }

        ActionUtils actionUtils = new ActionUtils(LanguageEnums.ZH);
        for (ActionUtils.ActionFlag flag : actionUtils.getFlags(type)) {
            System.out.println(flag.getFlagName());
        }
        System.out.println(((0x1 | 0x10) & 0x1) != 0);
    }
}
