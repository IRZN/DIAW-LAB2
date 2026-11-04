import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

/** Restringe o acesso a usuários autenticados e, opcionalmente, a um perfil. */
export default function ProtectedRoute({ perfil }) {
  const { autenticado, usuario } = useAuth()
  const location = useLocation()

  if (!autenticado) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }
  if (perfil && usuario.perfil !== perfil) {
    return <Navigate to="/" replace />
  }
  return <Outlet />
}
