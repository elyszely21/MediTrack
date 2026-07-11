import { useEffect, useState } from "react";
import axios from "../api/axios";

export default function Appointments() {
  const [appointments, setAppointments] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    patientId: "", appointmentDate: "",
    appointmentTime: "", status: "SCHEDULED", remarks: ""
  });

  const fetchAppointments = async () => {
    const res = await axios.get("/appointments");
    setAppointments(res.data);
  };

  useEffect(() => { fetchAppointments(); }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    await axios.post("/appointments", {
      ...form,
      patientId: Number(form.patientId),
      appointmentTime: form.appointmentTime + ":00"
    });
    setShowForm(false);
    fetchAppointments();
  };

  const statusColor = (s) => ({
    SCHEDULED: "bg-blue-100 text-blue-800",
    COMPLETED: "bg-green-100 text-green-800",
    CANCELLED: "bg-red-100 text-red-800"
  }[s] || "bg-gray-100");

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-2xl font-bold">Appointments</h1>
        <button onClick={() => setShowForm(true)}
          className="bg-blue-600 text-white px-4 py-2 rounded">
          + Schedule
        </button>
      </div>

      <table className="w-full border-collapse border border-gray-300">
        <thead className="bg-gray-100">
          <tr>
            {["Patient ID","Date","Time","Status","Remarks"].map(h => (
              <th key={h} className="border border-gray-300 p-2 text-left">{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {appointments.map(a => (
            <tr key={a.id} className="hover:bg-gray-50">
              <td className="border border-gray-300 p-2">{a.patientId}</td>
              <td className="border border-gray-300 p-2">{a.appointmentDate}</td>
              <td className="border border-gray-300 p-2">{a.appointmentTime?.slice(0,5)}</td>
              <td className="border border-gray-300 p-2">
                <span className={`px-2 py-1 rounded text-sm ${statusColor(a.status)}`}>
                  {a.status}
                </span>
              </td>
              <td className="border border-gray-300 p-2">{a.remarks}</td>
            </tr>
          ))}
        </tbody>
      </table>

      {showForm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
          <div className="bg-white p-6 rounded shadow-lg w-96">
            <h2 className="text-xl font-bold mb-4">Schedule Appointment</h2>
            <form onSubmit={handleSubmit} className="space-y-3">
              <input placeholder="Patient ID" value={form.patientId}
                onChange={e => setForm({...form, patientId: e.target.value})}
                className="border p-2 w-full rounded" />
              <input type="date" value={form.appointmentDate}
                onChange={e => setForm({...form, appointmentDate: e.target.value})}
                className="border p-2 w-full rounded" />
              <input type="time" value={form.appointmentTime}
                onChange={e => setForm({...form, appointmentTime: e.target.value})}
                className="border p-2 w-full rounded" />
              <select value={form.status}
                onChange={e => setForm({...form, status: e.target.value})}
                className="border p-2 w-full rounded">
                {["SCHEDULED","COMPLETED","CANCELLED"].map(s => (
                  <option key={s}>{s}</option>
                ))}
              </select>
              <input placeholder="Remarks" value={form.remarks}
                onChange={e => setForm({...form, remarks: e.target.value})}
                className="border p-2 w-full rounded" />
              <div className="flex gap-2">
                <button type="submit" className="bg-blue-600 text-white px-4 py-2 rounded">Save</button>
                <button type="button" onClick={() => setShowForm(false)}
                  className="bg-gray-300 px-4 py-2 rounded">Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}