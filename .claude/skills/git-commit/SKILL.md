---
name: git-commit
description: 提交仓库代码：git add 全部改动（含未跟踪的新建文件）后 commit，只提交不推送。当用户说"提交""提交代码""commit"或要求把当前改动入库时使用。
---

# 提交仓库代码（只提交，不推送）

把当前工作区的**所有**改动（已修改、已删除、新增/未跟踪文件）一起暂存并提交到 git。**只 commit，绝不 push**。

## 适用场景

- 用户说"提交"、"提交代码"、"commit"
- 需要把当前所有改动一次性入库（含未 add 的新建文件）

## 前置要求

- 所有命令在**仓库根目录**执行（`E:\Work\moneynote\moneynote`），若当前目录不在仓库根，先 `cd` 过去或用 `git -C`。
- Commit message 优先取 skill 传入的参数（`args`）；未传时根据变更内容自动归纳。

## 步骤

### 1. 展示变更摘要（提交前必须先展示）

```bash
git status --porcelain
git diff --stat
```

- 列出所有 `M`（修改）、`D`（删除）、`??`（未跟踪/新增）的文件，向用户展示变更摘要。
- 若有新增文件（`??`），明确指出它们会被一并 `git add`。
- **若工作区干净（无任何变更）**：告知用户"没有可提交的变更"，停止，不创建空提交。

### 2. 检查是否有敏感/危险文件

若变更列表中出现以下内容，**停下来向用户确认后再继续**，不要直接提交：

- `.env`、密钥、token、证书、`settings.json` 等配置/凭据文件
- CI/CD 配置（`.github/workflows` 等）
- 明显是构建产物/本地临时文件（如 `dist/`、`.umi/`、`*.tmp`、日志）——先指出，确认是否真要提交
- 大量无关文件（超出本次工作范围的批量变更）

### 3. 暂存所有变更

```bash
git add -A
```

- `-A` 同时暂存：修改、删除、新增/未跟踪文件，满足"没 add 的新建文件也要 add"。
- 暂存后复核 `git status --porcelain`，确认暂存区内容与展示的摘要一致。

### 4. 提交

```bash
git commit -m "<message>"
```

- Commit message：简洁英文，遵循 conventional commits（`feat:` / `fix:` / `chore:` / `refactor:` / `docs:` 等），可带 scope，如 `feat(auth): add login validation`。
- 结尾追加 `Co-Authored-By: Claude <noreply@anthropic.com>`。
- 提交后展示结果：

```bash
git log --oneline -1
git show --stat --oneline HEAD | head -30
```

### 5. 汇报

- 提交的 commit hash 与 message
- 本次提交包含的文件与变更统计
- **明确说明：未推送**，本地领先远程的提交数，需要推送时再让用户明确要求

## 红线

- **绝不 `git push`**——本 skill 只提交不推送，即使本地领先远程也保持不推送。
- 不执行 `git rebase` / `git reset --hard` / 强制推送等危险操作。
- 提交前必须展示变更摘要；发现敏感文件或无关变更先指出并确认，不静默提交。
- 不修改/丢弃提交历史。
- 若用户要求推送，用 `git push` 工具命令单独执行，不写进本 skill 的常规流程。
