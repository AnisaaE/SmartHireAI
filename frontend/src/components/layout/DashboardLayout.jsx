import { Outlet } from 'react-router-dom';
import Navbar from './Navbar';
import Sidebar from './Sidebar';
import CustomGraphics from '../common/CustomGraphics';
import './DashboardLayout.css';

export default function DashboardLayout() {
  return (
    <div className="dashboard-layout">
      <CustomGraphics variant="dashboard" className="dashboard-shell-graphics" />
      <Navbar />
      <Sidebar />
      <main className="dashboard-main">
        <div className="dashboard-frame">
          <div className="dashboard-frame-corner dashboard-frame-corner-tl" />
          <div className="dashboard-frame-corner dashboard-frame-corner-tr" />
          <div className="dashboard-frame-corner dashboard-frame-corner-bl" />
          <div className="dashboard-frame-corner dashboard-frame-corner-br" />
          <Outlet />
        </div>
      </main>
    </div>
  );
}
