import { Navigate, Outlet } from 'react-router-dom';

// Gate for pages that hold clinical/staff data. A PATIENT account is
// authenticated (passes ProtectedRoute) but must never reach these —
// the backend already blocks the API calls, this just avoids showing
// a logged-in patient a broken staff page and sends them somewhere sane.
const StaffRoute = () => {
  const storedUser = localStorage.getItem('meditrackUser');
  const user = storedUser ? JSON.parse(storedUser) : null;

  if (user?.role === 'PATIENT') {
    return <Navigate to="/patient/dashboard" replace />;
  }

  return <Outlet />;
};

export default StaffRoute;