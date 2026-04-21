## Context

TinyFlow 项目有两个独立的后端实现：
- **Python 后端** (`backend/`): 基于 FastAPI + LangGraph 的完整实现
- **Java 后端** (`tinyflow-backend/`): 基于 Spring Boot + LangGraph4J 的移植实现

需要系统性地对比两个版本的功能一致性，识别差异并制定补齐方案。

## Goals / Non-Goals

**Goals:**
- 全面审计 Python 后端的所有功能模块和 API
- 对比 Java 后端实现，识别功能缺失
- 创建详细的差异报告和优先级排序
- 制定 Java 后端功能补齐的路线图

**Non-Goals:**
- 不直接修改 Java 后端代码（仅做分析）
- 不涉及前端功能对比
- 不修改 Python 后端实现

## Decisions

### 对比维度
1. **API 端点**: URL、方法、请求/响应格式
2. **核心功能**: Graph 节点、路由逻辑、中间件
3. **数据模型**: 状态管理、持久化
4. **事件流**: SSE 事件类型和格式
5. **技能系统**: 技能注册、路由、执行

### 对比方法
- 代码审查：逐文件对比实现
- API 映射：建立端点到端点的映射表
- 功能矩阵：用表格展示功能覆盖情况

## Risks / Trade-offs

**Risk**: Java 后端基于 LangGraph4J，与 Python LangGraph 存在 API 差异
→ **Mitigation**: 重点关注功能等价性，而非实现细节一致性

**Risk**: Python 后端持续演进，对比结果可能快速过时
→ **Mitigation**: 记录对比时的代码版本（commit hash）

## Migration Plan

本变更仅产生分析报告，不涉及代码迁移。

## Open Questions

1. Java 后端的优先级是什么？是否需要完全对齐 Python 功能？
2. 是否有计划逐步淘汰其中一个后端实现？
