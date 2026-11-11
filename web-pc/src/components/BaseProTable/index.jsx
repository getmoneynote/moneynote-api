import { ProTable } from '@ant-design/pro-components';

/**
 * 统一数据表格壳：所有业务列表统一走这里，公共行为集中管理。
 *
 * 这里是 antd 与后端分页契约之间的唯一转换边界（API 层不做任何分页转换）：
 * - 出参：ProTable 的 current（1 起）/ pageSize → 后端的 page（0 起）/ size，其余查询条件透传
 * - 入参：后端分页结构 { content, current, pageSize, total } → ProTable 的 { data, total, success }
 * - 排序：ProTable 的 sort 是 { 字段: 'ascend' | 'descend' }，转成 Spring Pageable 的 sort=字段,asc|desc。
 *   字段值默认取列的 dataIndex（必须与后端实体属性名一致）；
 *   列表 dataIndex 与实体属性不一致时（如 DTO 里的 currencyName 对应 currency.name），
 *   页面可通过 sortMap 显式映射：{ 列 dataIndex: 实体属性路径 }。
 *
 * 页面的 request 函数允许返回两种结构：
 * - 后端分页结构（即 xxxApi.list 的返回值），由这里映射成 ProTable 结构；
 * - ProTable 结构 { data, success, total? }（非分页列表、空态短路等），原样透传给 ProTable。
 */
export default function BaseProTable(props) {
  const { pagination, options, request, sortMap, ...rest } = props;

  const wrappedRequest = request
    ? async (params, sort, filter) => {
        const { current, pageSize, ...query } = params || {};
        // 排序字段默认用 dataIndex，dataIndex 与实体属性不一致时经 sortMap 映射（如 currencyName → currency.name）
        const sortEntry = Object.entries(sort || {}).find(([, dir]) => dir);
        const sortField = sortEntry ? (sortMap?.[sortEntry[0]] ?? sortEntry[0]) : undefined;
        const result = await request(
          {
            ...query,
            ...(current !== undefined ? { page: current - 1 } : {}),
            ...(pageSize !== undefined ? { size: pageSize } : {}),
            ...(sortField
              ? { sort: `${sortField},${sortEntry[1] === 'descend' ? 'desc' : 'asc'}` }
              : {}),
          },
          sort,
          filter,
        );
        if (result && Array.isArray(result.content)) {
          return { data: result.content, total: result.total, success: true };
        }
        return result;
      }
    : undefined;

  return (
    <ProTable
      {...rest}
      request={wrappedRequest}
      rowKey="id"
      bordered
      pagination={{
        // defaultPageSize: 10,
        // showSizeChanger: true,
        // showQuickJumper: true,
        ...pagination,
      }}
      options={{
        // density: false,
        // fullScreen: false,
        // reload: false,
        // setting: false,
        ...options,
      }}
    />
  );
}
