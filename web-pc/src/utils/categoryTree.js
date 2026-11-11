/** CategoryTreeNode（后端 /categories/tree）→ antd TreeSelect 树数据。 */
export function toCategoryTreeData(nodes) {
  return (nodes || []).map((n) => ({
    title: n.name,
    value: n.id,
    children: n.children?.length ? toCategoryTreeData(n.children) : undefined,
  }));
}
