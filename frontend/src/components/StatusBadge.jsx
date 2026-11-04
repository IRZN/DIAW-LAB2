import { STATUS } from '../utils/format'

export default function StatusBadge({ status }) {
  const info = STATUS[status] || { label: status }
  return (
    <span className={`badge badge-${status?.toLowerCase()}`} title={info.descricao}>
      <i />
      {info.label}
    </span>
  )
}
