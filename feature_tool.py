#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
feature_tool.py —— 功能清单驱动的代码生成器
================================================================
命令：
  python feature_tool.py           显示帮助
  python feature_tool.py list      列出所有功能
  python feature_tool.py add       交互式添加一个功能
  python feature_tool.py gen       从 features.txt 生成 Kotlin

数据源：features.txt
生成物：
  app/src/main/kotlin/com/myxptemplate/data/Feature.kt
  app/src/main/kotlin/com/myxptemplate/data/Tabs.kt
"""

import os
import sys

ROOT          = os.path.dirname(os.path.abspath(__file__))
FEATURES_FILE = os.path.join(ROOT, "features.txt")
FEATURE_KT    = os.path.join(ROOT, "app/src/main/kotlin/com/myxptemplate/data/Feature.kt")
TABS_KT       = os.path.join(ROOT, "app/src/main/kotlin/com/myxptemplate/data/Tabs.kt")

HEADER = "// ⚠️ 本文件由 feature_tool.py 从 features.txt 生成，不要手改。\n" \
         "//    修改功能 → 改 features.txt → 跑 python feature_tool.py gen\n\n"


# ════════════════════════════════════════════════════════════════
#  解析
# ════════════════════════════════════════════════════════════════

def parse():
    """返回 (tabs, funcs)。tabs: [(name,zh,en,icon)]，funcs: [(...)]"""
    if not os.path.exists(FEATURES_FILE):
        print("找不到 features.txt，请先跑 patch_typing_boost.py")
        sys.exit(1)

    tabs, funcs = [], []
    with open(FEATURES_FILE, "r", encoding="utf-8") as f:
        for raw in f:
            line = raw.split("#", 1)[0].strip()
            if not line:
                continue
            parts = [p.strip() for p in line.split("|")]
            if parts[0] == "@tab":
                # @tab | Home | 主页 | Home | texture.png
                if len(parts) < 5:
                    print("坏行（@tab 需要 5 段）:", line); sys.exit(1)
                _, name, zh, en, icon = parts[:5]
                tabs.append((name, zh, en, icon))
            else:
                # Name | zh | en | zhDesc | enDesc | kind | tab
                if len(parts) < 7:
                    print("坏行（func 需要 7 段）:", line); sys.exit(1)
                name, zh, en, dzh, den, kind, tab = parts[:7]
                kind = kind.lower()
                if kind not in ("toggle", "action"):
                    print("kind 必须是 toggle / action:", line); sys.exit(1)
                funcs.append((name, zh, en, dzh, den, kind, tab.lower()))
    return tabs, funcs


def esc(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"')


# ════════════════════════════════════════════════════════════════
#  生成 Feature.kt
# ════════════════════════════════════════════════════════════════

def gen_feature_kt(tabs, funcs):
    lines = []
    lines.append(HEADER)
    lines.append("package com.myxptemplate.data\n\n")
    lines.append("/** 所有功能的单一定义源；由 features.txt 生成。 */\n")
    lines.append("enum class Feature(\n")
    lines.append("    val labelEn: String,\n")
    lines.append("    val labelZh: String,\n")
    lines.append("    val descEn:  String = \"\",\n")
    lines.append("    val descZh:  String = \"\",\n")
    lines.append("    val kind:    Kind   = Kind.Toggle\n")
    lines.append(") {\n")

    # Tab 枚举项
    lines.append("    // ── Tab 页 ──\n")
    for name, zh, en, _ in tabs:
        lines.append(f'    {name}("{esc(en)}", "{esc(zh)}"),\n')
    lines.append("\n")

    # 每个 tab 下的功能
    for tab_name, _, _, _ in tabs:
        key = tab_name.lower()
        rows = [f for f in funcs if f[6] == key]
        if not rows:
            continue
        lines.append(f"    // ── {tab_name} ──\n")
        for i, (name, zh, en, dzh, den, kind, _t) in enumerate(rows):
            last = (i == len(rows) - 1) and (tab_name == tabs[-1][0])
            sep = "" if last else ","
            kind_kotlin = "Kind.Action" if kind == "action" else "Kind.Toggle"
            lines.append(
                f'    {name}("{esc(en)}", "{esc(zh)}", '
                f'"{esc(den)}", "{esc(dzh)}", {kind_kotlin}){sep}\n'
            )
        lines.append("\n")

    lines.append("    ;\n\n")
    lines.append("    enum class Kind { Toggle, Action }\n\n")
    lines.append("    companion object {\n")
    lines.append("        fun of(name: String): Feature? = entries.firstOrNull { it.name == name }\n")
    lines.append("    }\n")
    lines.append("}\n")

    with open(FEATURE_KT, "w", encoding="utf-8", newline="\n") as f:
        f.write("".join(lines))
    print("[GEN] " + os.path.relpath(FEATURE_KT, ROOT))


# ════════════════════════════════════════════════════════════════
#  生成 Tabs.kt
# ════════════════════════════════════════════════════════════════

def gen_tabs_kt(tabs, funcs):
    lines = []
    lines.append(HEADER)
    lines.append("package com.myxptemplate.data\n\n")
    lines.append("/** Tab 结构与成员归属；由 features.txt 生成。 */\n")
    lines.append("object Tabs {\n\n")
    lines.append("    data class TabDef(\n")
    lines.append("        val feature: Feature,\n")
    lines.append("        val icon:    String,\n")
    lines.append("        val actions: List<Feature> = emptyList(),\n")
    lines.append("        val toggles: List<Feature> = emptyList()\n")
    lines.append("    )\n\n")
    lines.append("    val LIST: List<TabDef> = listOf(\n")

    for name, _zh, _en, icon in tabs:
        key = name.lower()
        acts = [f[0] for f in funcs if f[6] == key and f[5] == "action"]
        tgs  = [f[0] for f in funcs if f[6] == key and f[5] == "toggle"]
        if not acts and not tgs:
            lines.append(f'        TabDef(Feature.{name}, "{icon}"),\n')
        else:
            lines.append(f'        TabDef(\n')
            lines.append(f'            Feature.{name}, "{icon}",\n')
            if acts:
                lines.append(f'            actions = listOf({", ".join("Feature." + a for a in acts)}),\n')
            if tgs:
                lines.append(f'            toggles = listOf({", ".join("Feature." + t for t in tgs)})\n')
            lines.append(f'        ),\n')
    lines.append("    )\n")
    lines.append("}\n")

    with open(TABS_KT, "w", encoding="utf-8", newline="\n") as f:
        f.write("".join(lines))
    print("[GEN] " + os.path.relpath(TABS_KT, ROOT))


# ════════════════════════════════════════════════════════════════
#  命令
# ════════════════════════════════════════════════════════════════

def cmd_list():
    tabs, funcs = parse()
    cur = None
    for name, zh, en, _dzh, _den, kind, tab in funcs:
        if tab != cur:
            print(f"\n[{tab}]")
            cur = tab
        print(f"  {name:14s}  {kind:6s}  {zh} / {en}")


def cmd_gen():
    tabs, funcs = parse()
    gen_feature_kt(tabs, funcs)
    gen_tabs_kt(tabs, funcs)


def cmd_add():
    tabs, _ = parse()
    tab_keys = [t[0].lower() for t in tabs]
    print("可用 Tab: " + ", ".join(tab_keys))
    name = input("枚举名（如 AutoJump）: ").strip()
    if not name or not name[0].isupper():
        print("枚举名必须大写开头"); return
    zh   = input("中文标签: ").strip()
    en   = input("英文标签: ").strip() or name
    dzh  = input("中文描述: ").strip()
    den  = input("英文描述: ").strip() or dzh
    kind = (input("类型 [toggle/action]（默认 toggle）: ").strip() or "toggle").lower()
    if kind not in ("toggle", "action"):
        print("kind 必须是 toggle / action"); return
    tab  = (input(f"属于哪个 tab（默认 {tab_keys[0]}）: ").strip() or tab_keys[0]).lower()
    if tab not in tab_keys:
        print("未知 tab"); return

    # append 到 features.txt 末尾
    line = f"{name} | {zh} | {en} | {dzh} | {den} | {kind} | {tab}\n"
    with open(FEATURES_FILE, "a", encoding="utf-8", newline="\n") as f:
        f.write(line)
    print("[ADD]  " + line.strip())
    cmd_gen()


def cmd_help():
    print(__doc__)
    print("文件位置：")
    print("  数据源: " + os.path.relpath(FEATURES_FILE, ROOT))
    print("  生成物: " + os.path.relpath(FEATURE_KT, ROOT))
    print("          " + os.path.relpath(TABS_KT, ROOT))


def main():
    args = sys.argv[1:]
    cmd  = args[0] if args else "help"
    if   cmd == "list": cmd_list()
    elif cmd == "gen":  cmd_gen()
    elif cmd == "add":  cmd_add()
    else:               cmd_help()


if __name__ == "__main__":
    main()
