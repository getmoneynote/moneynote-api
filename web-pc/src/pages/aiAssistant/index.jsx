import { useEffect, useRef, useState } from "react";
import { DeleteOutlined } from '@ant-design/icons';
import { App, Button, Card, Space, Spin } from "antd";
import { PageContainer } from '@ant-design/pro-components';
import { Actions, Bubble, CodeHighlighter, Sender, Think, XProvider } from "@ant-design/x";
import XMarkdown from '@ant-design/x-markdown';
import { messageApi, ChatEventType } from '@/api/message';
import { useT } from '@/utils/i18n';
import ChatBlock from './blocks/ChatBlock';
import { useStyles } from "./style";

// 与后端 Message.ROLE_USER 对齐；其余一律视为助手
const ROLE_USER = 1;

// XMarkdown 自定义 code 渲染：块级代码用 CodeHighlighter（语言标签头 + 复制按钮 +
// 独立背景/边框主题），行内代码保持原生样式。组件必须定义在模块级保持引用稳定，
// 内联定义会导致流式期间 code 元素反复重挂载
const MarkdownCode = ({ block, lang, children, domNode, streamStatus, ...rest }) => (
  block
    ? <CodeHighlighter lang={lang}>{String(children)}</CodeHighlighter>
    : <code {...rest}>{children}</code>
);

const markdownComponents = { code: MarkdownCode };

/**
 * 单个 Agent 的固定时间线页面：一个菜单项 = 一个 (用户, agentCode) 分区，
 * 没有会话列表、没有新建/切换/删除会话。四个子路由各用 wrapper 传入 agent，
 * 切换菜单时组件重新挂载，状态天然隔离。
 */
export default ({ agent }) => {

  const { styles } = useStyles();
  const t = useT();
  // 流式发送走 fetch 不经全局 errorHandler，失败提示由页面自行展示；
  // 静态 modal 同理不受 ConfigProvider 控制，统一走 App.useApp()
  const { message, modal } = App.useApp();
  const agentCode = agent.code;

  const [inputValue, setInputValue] = useState('');
  // 当前 Agent 时间线的服务端消息。只在两个时机加载：进入菜单、一轮对话结束。
  // 发送期间列表保持不动，与轮次的本地渲染在时间上天然错开，不存在竞态
  const [messages, setMessages] = useState([]);
  const [loadingMessages, setLoadingMessages] = useState(true);
  // 进行中的一轮对话：用户消息回显 + 流式回复（reasoning/content/blocks/error），null = 空闲。
  // 它是发送期间界面渲染的唯一来源；轮次结束统一刷新服务端数据替换
  const [activeRound, setActiveRound] = useState(null);
  const [feedbackState, setFeedbackState] = useState({});
  const abortRef = useRef(null);
  // 当前 handleSend 整段流程的 Promise：清空记录前必须等它结束（含终态落库与列表刷新）
  const runRef = useRef(null);
  // 轮次自增 id：迟到的流事件/清理只作用于自己那一轮，不会波及新一轮
  const roundIdRef = useRef(0);
  // 消息列表请求序号：切换菜单/清空时丢弃过期响应
  const loadSeqRef = useRef(0);

  // 拉取该 Agent 时间线的历史。
  // showLoading：进入菜单时展示加载态；轮次收尾时不展示——保持当前画面，
  // 服务端数据到达后与轮次 UI 在同一次更新里原子替换（先清会留出空窗导致一闪）。
  // settleRoundId：传入时在数据就绪后清理对应轮次。
  // 【后期扩展点 · 上下文窗口】当前是全量历史；将来后端加窗口/分页时，
  // 这里要同步只取最近 N 条，保证用户看到的与模型看到的是同一段历史。
  const loadMessages = async ({ showLoading = true, settleRoundId } = {}) => {
    const seq = ++loadSeqRef.current;
    if (showLoading) {
      setLoadingMessages(true);
    }
    try {
      const list = await messageApi.list(agentCode);
      if (seq === loadSeqRef.current) {
        setMessages(list || []);
        if (settleRoundId != null) {
          setActiveRound((r) => (r?.id === settleRoundId ? null : r));
        }
      }
    } catch {
      // 错误提示走 request 全局 errorHandler，这里只保证列表复位
      if (seq === loadSeqRef.current) {
        setMessages([]);
        if (settleRoundId != null) {
          setActiveRound((r) => (r?.id === settleRoundId ? null : r));
        }
      }
    } finally {
      if (showLoading && seq === loadSeqRef.current) {
        setLoadingMessages(false);
      }
    }
  };

  useEffect(() => {
    loadMessages();
    // 切换菜单 / 卸载时中止进行中的流；已生成部分由后端 doOnCancel 落库，不丢失
    return () => {
      abortRef.current?.abort();
    };
    // agentCode 由 wrapper 固定传入，不同子路由是不同的组件实例
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [agentCode]);

  // 消息点赞/点踩的界面状态（后端暂无反馈存储），key 为消息 id 或轮次 key
  const toggleFeedback = (key, value) => {
    setFeedbackState((prev) => ({ ...prev, [key]: prev[key] === value ? 'default' : value }));
  };

  // AI 消息底部操作栏：复制原文（Actions.Copy 自带复制成功反馈）+ 点赞/点踩
  const aiFooter = (key, text) => (
    <Space size={2}>
      <Actions.Copy text={text} />
      <Actions.Feedback
        value={feedbackState[key] || 'default'}
        onChange={(value) => toggleFeedback(key, value)}
      />
    </Space>
  );

  // AI 气泡的 content 是结构化对象 {reasoning, text, streaming, loading, waiting}。
  // 一个分区内 agentCode 恒定，所以不再显示 Agent 徽标。
  // waiting 由「本轮还没有任何正文/思考/错误」派生：覆盖提交到首个 token 的空窗，
  // 不依赖某个事件分支记得清标记，历史消息共用同一渲染路径也不会显示等待态。
  const activeWaiting = !!activeRound
    && !activeRound.error
    && !activeRound.reasoning
    && !activeRound.content
    // 只发块、还没发正文时也算「有内容了」，否则块会被等待态盖住
    && !activeRound.blocks.length;
  const bubbleItems = [
    ...messages.map((m) => (
      m.role === ROLE_USER
        ? { key: String(m.id), role: 'user', content: m.content }
        : {
            key: String(m.id),
            role: 'ai',
            content: {
              reasoning: m.reasoningContent,
              text: m.content,
              // 展示块（图表/表单）：历史消息由 parts 下发，与流式轮次共用同一渲染路径
              blocks: m.parts || [],
              messageId: m.id,
              streaming: false,
              loading: false,
            },
            footer: aiFooter(String(m.id), m.content),
          }
    )),
    ...(activeRound ? [
      { key: 'round-user', role: 'user', content: activeRound.userText },
      {
        key: 'round-ai',
        role: 'ai',
        content: {
          reasoning: activeRound.reasoning,
          text: activeRound.error
            ? `**${t('aiAssistant.stream.failed')}**：${activeRound.error}`
            : activeRound.content,
          blocks: activeRound.blocks,
          // 表单提交需要 messageId（块 id 只在消息内唯一），由 start 事件带出
          messageId: activeRound.messageId,
          streaming: !activeRound.error,
          // 首个 token 未到时 Think 显示转圈占位
          loading: !activeRound.error && !activeRound.reasoning && !activeRound.content
            && !activeRound.blocks.length,
          waiting: activeWaiting,
        },
        footer: aiFooter('round-ai', activeRound.content),
      },
    ] : []),
  ];

  // 表单块提交成功后刷新服务端消息：块的 status 是服务端事实，刷新后统一显示「已提交」
  const handleBlockSubmitted = () => {
    loadMessages({ showLoading: false });
  };

  const handleSend = async (content) => {
    const text = content.trim();
    if (!text || activeRound) return;
    const roundId = ++roundIdRef.current;
    const controller = new AbortController();
    abortRef.current = controller;
    setInputValue('');
    setActiveRound({
      id: roundId, userText: text, reasoning: '', content: '', blocks: [], messageId: null, error: null,
    });

    // 整段流程（含 finally 的列表刷新）暴露给「清空记录」等待，避免竞态
    const run = (async () => {
      try {
        await messageApi.send(agentCode, { content: text }, {
          onEvent: ({ eventType, messageId, eventData }) => {
            switch (eventType) {
              case ChatEventType.START:
                // start 携带菜单选定的 agentCode，可校验响应没有串到别的分区；
                // 身份已经由菜单确定，前端无需再等待路由结果
                if (eventData?.agentCode && eventData.agentCode !== agentCode) {
                  console.warn('[aiAssistant] agentCode 与当前菜单不一致', eventData.agentCode, agentCode);
                }
                // 记下本次回复的消息 id：表单块提交按「消息 id + 块 id」定位
                setActiveRound((r) => (r?.id === roundId ? { ...r, messageId } : r));
                break;
              case ChatEventType.REASONING:
                setActiveRound((r) => (r?.id === roundId ? { ...r, reasoning: r.reasoning + eventData } : r));
                break;
              case ChatEventType.CONTENT:
                setActiveRound((r) => (r?.id === roundId ? { ...r, content: r.content + eventData } : r));
                break;
              case ChatEventType.BLOCK:
                // 展示块（图表/表单）独立累积：不进 Markdown 正文，由块组件渲染
                setActiveRound((r) => (r?.id === roundId ? { ...r, blocks: [...r.blocks, eventData] } : r));
                break;
              case ChatEventType.ERROR:
                setActiveRound((r) => (r?.id === roundId
                  ? { ...r, error: eventData?.message || eventData?.code || t('aiAssistant.stream.failed') }
                  : r));
                break;
              default:
                break;
            }
          },
          signal: controller.signal,
        });
      } catch (e) {
        if (e.name === 'AbortError') {
          // 中止：后端 doOnCancel 落库 STOPPED + 已生成部分，finally 照常刷新服务端数据
        } else {
          message.error(e.message);
          // 恢复输入内容便于修改后重发
          setInputValue(text);
        }
      } finally {
        abortRef.current = null;
        await loadMessages({ showLoading: false, settleRoundId: roundId });
      }
    })();
    runRef.current = run;
    await run;
    if (runRef.current === run) {
      runRef.current = null;
    }
  };

  // 清空记录：替代原来的「删除会话」。先中止并等当前轮整段流程结束再删除，
  // 否则终态落库回调会在删除后写出幽灵消息；后端 persistTerminal 也有静默跳过兜底。
  const clearHistory = () => {
    modal.confirm({
      title: t('aiAssistant.clear.confirm'),
      okButtonProps: { danger: true },
      onOk: async () => {
        abortRef.current?.abort();
        await runRef.current?.catch(() => {});
        await messageApi.clear(agentCode);
        ++loadSeqRef.current; // 作废任何在途的历史请求
        abortRef.current = null;
        setMessages([]);
        setActiveRound(null);
        setFeedbackState({});
        setInputValue('');
        message.success(t('operation.success'));
      },
    });
  };

  // 输入框在欢迎页与对话页共用，仅容器布局不同
  const sender = (
    <Sender
      value={inputValue}
      onChange={setInputValue}
      onSubmit={handleSend}
      onCancel={() => abortRef.current?.abort()}
      loading={!!activeRound}
      placeholder={t(agent.hintKey)}
      autoSize={{ minRows: 4, maxRows: 8 }}
      style={{ maxWidth: 940, width: '100%' }}
      styles={{ input: { paddingBlock: 0 } }}
    />
  );

  const isWelcome = !loadingMessages && messages.length === 0 && !activeRound;

  return (
    <PageContainer
      ghost
      extraContent={
        <Button
          icon={<DeleteOutlined />}
          onClick={clearHistory}
          disabled={!messages.length && !activeRound}
        >
          {t('aiAssistant.clear')}
        </Button>
      }
      childrenContentStyle={{
        paddingBlock: 0,
        height: 'calc(100vh - 180px)',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden',
      }}
    >
      <Card
        style={{
          height: '100%',
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
        }}
        styles={{
          body: {
            flex: 1,
            padding: 0,
            overflow: 'hidden',
            display: 'flex',
            flexDirection: 'column',
          },
        }}
      >
        <XProvider>
          <div className={styles.layout}>
            {isWelcome ? (
              // 欢迎页：该 Agent 的时间线为空时显示，标题/提示按菜单对应文案
              <div className={styles.footerCenter}>
                <div className={styles.welcomeTitle}>{t(agent.welcomeKey)}</div>
                <div className={styles.welcomeHint}>{t(agent.hintKey)}</div>
                {sender}
              </div>
            ) : (
              <>
                <div className={styles.messages}>
                  {loadingMessages ? (
                    <Spin className={styles.loading} />
                  ) : (
                    <Bubble.List
                      autoScroll
                      role={{
                        user: { placement: 'end' },
                        ai: {
                          variant: 'borderless',
                          contentRender: (content) => (
                            content.waiting ? (
                              <div className={styles.generatingHint}>
                                <Spin size="small" />
                                {t('aiAssistant.generating')}
                              </div>
                            ) : (
                              <>
                                {(content.reasoning || content.streaming) && (
                                  <Think
                                    title={content.streaming
                                      ? t('aiAssistant.thinking')
                                      : t('aiAssistant.thinking.process')}
                                    loading={content.loading}
                                    blink={content.streaming && !content.loading}
                                    defaultExpanded={content.streaming}
                                  >
                                    <XMarkdown content={content.reasoning} components={markdownComponents} />
                                  </Think>
                                )}
                                <XMarkdown content={content.text} components={markdownComponents} />
                                {/* 结构化展示块排在正文之后（后端保证块事件在文本流之后到达） */}
                                {content.blocks?.length > 0
                                  && content.blocks.map((block) => (
                                    <ChatBlock
                                      key={block.id}
                                      block={block}
                                      agentCode={agentCode}
                                      messageId={content.messageId}
                                      onSubmitted={handleBlockSubmitted}
                                    />
                                  ))}
                              </>
                            )
                          ),
                        },
                      }}
                      styles={{ root: { maxWidth: 940 } }}
                      items={bubbleItems}
                    />
                  )}
                </div>

                <div className={styles.footer}>
                  {sender}
                </div>
              </>
            )}
          </div>
        </XProvider>
      </Card>
    </PageContainer>
  );
};
