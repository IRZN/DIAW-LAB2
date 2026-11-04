export default function Alert({ tipo = 'erro', children }) {
  if (!children) return null
  return (
    <div className={`alert alert-${tipo}`} role={tipo === 'erro' ? 'alert' : 'status'}>
      {children}
    </div>
  )
}
