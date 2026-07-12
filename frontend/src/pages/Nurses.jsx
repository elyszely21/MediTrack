import { useEffect, useState } from "react";
import Navbar from "../components/Navbar";
import axios from "../api/axios";

export default function Nurses() {
  const user    = JSON.parse(localStorage.getItem("meditrackUser") || "{}");
  const isAdmin = user?.role === "SUPER_ADMIN";

  const [nurses, setNurses]           = useState([]);
  const [loading, setLoading]         = useState(true);
  const [error, setError]             = useState("");
  const [showForm, setShowForm]       = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [submitting, setSubmitting]   = useState(false);
  const [successMsg, setSuccessMsg]   = useState("");
  const [form, setForm] = useState({
    fullName: "", email: "",
    phoneNumber: "", password: "",
    confirmPassword: ""
  });
  const [formError, setFormError] = useState("");

  const fetchNurses = async () => {
    setLoading(true);
    setError("");
    try {
      const res = await axios.get("/users/nurses");
      setNurses(res.data);
    } catch {
      setError("Failed to load nurses.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchNurses(); }, []);

  const resetForm = () => {
    setForm({
      fullName: "", email: "",
      phoneNumber: "", password: "",
      confirmPassword: ""
    });
    setFormError("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError("");

    if (form.password !== form.confirmPassword) {
      setFormError("Passwords do not match.");
      return;
    }
    if (form.password.length < 6) {
      setFormError("Password must be at least 6 characters.");
      return;
    }

    setSubmitting(true);
    try {
      await axios.post("/auth/admin/register-nurse", {
        fullName:        form.fullName,
        email:           form.email,
        phoneNumber:     form.phoneNumber,
        password:        form.password,
        confirmPassword: form.confirmPassword
      });
      setShowForm(false);
      resetForm();
      setSuccessMsg("Nurse registered successfully.");
      setTimeout(() => setSuccessMsg(""), 3000);
      fetchNurses();
    } catch (e) {
      setFormError(e.response?.data?.message || "Failed to register nurse.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async () => {
    try {
      await axios.delete(`/users/${deleteTarget.id}`);
      setDeleteTarget(null);
      setSuccessMsg("Nurse removed successfully.");
      setTimeout(() => setSuccessMsg(""), 3000);
      fetchNurses();
    } catch {
      alert("Failed to remove nurse.");
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="max-w-6xl mx-auto px-6 py-8">

        {/* Header */}
        <div className="flex justify-between items-start mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-800">Nurses</h1>
            <p className="text-gray-500 text-sm mt-1">
              Manage clinic nurse accounts
            </p>
          </div>
          {isAdmin && (
            <button
              onClick={() => { resetForm(); setShowForm(true); }}
              className="bg-blue-600 text-white px-4 py-2 rounded-lg
                text-sm hover:bg-blue-700"
            >
              + Register Nurse
            </button>
          )}
        </div>

        {/* Success message */}
        {successMsg && (
          <div className="bg-green-50 border border-green-200 text-green-700
            rounded-lg p-3 mb-4 text-sm">
            ✅ {successMsg}
          </div>
        )}

        {/* Error message */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-600
            rounded-lg p-3 mb-4 text-sm flex justify-between">
            <span>{error}</span>
            <button onClick={fetchNurses} className="underline">Retry</button>
          </div>
        )}

        {/* Stats bar */}
        <div className="bg-white border border-gray-200 rounded-xl p-4
          mb-6 flex items-center gap-6 shadow-sm">
          <div className="text-center">
            <p className="text-2xl font-bold text-blue-600">{nurses.length}</p>
            <p className="text-xs text-gray-500">Total Nurses</p>
          </div>
          <div className="h-8 w-px bg-gray-200" />
          <div className="text-center">
            <p className="text-2xl font-bold text-green-600">
              {nurses.filter(n => n.role === "ROLE_NURSE").length}
            </p>
            <p className="text-xs text-gray-500">Active</p>
          </div>
        </div>

        {/* Loading skeleton */}
        {loading ? (
          <div className="space-y-3">
            {[1,2,3].map(i => (
              <div key={i}
                className="h-20 bg-gray-200 animate-pulse rounded-xl" />
            ))}
          </div>
        ) : nurses.length === 0 ? (
          <div className="text-center py-16 text-gray-400">
            <p className="text-5xl mb-3">👩‍⚕️</p>
            <p className="font-medium text-gray-500">No nurses registered yet</p>
            <p className="text-sm mt-1">
              Click Register Nurse to add clinic staff
            </p>
          </div>
        ) : (
          /* Nurses table */
          <div className="bg-white border border-gray-200 rounded-xl
            shadow-sm overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  {["#","Name","Email","Phone","Joined",""].map(h => (
                    <th key={h}
                      className="text-left p-4 text-gray-600 font-medium">
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {nurses.map((n, i) => (
                  <tr key={n.id} className="hover:bg-gray-50">
                    <td className="p-4 text-gray-400">{i + 1}</td>
                    <td className="p-4">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 bg-purple-100 rounded-full
                          flex items-center justify-center text-purple-600
                          font-semibold text-sm">
                          {n.fullName?.charAt(0).toUpperCase()}
                        </div>
                        <span className="font-medium text-gray-800">
                          {n.fullName}
                        </span>
                      </div>
                    </td>
                    <td className="p-4 text-gray-600">{n.email}</td>
                    <td className="p-4 text-gray-600">{n.phoneNumber}</td>
                    <td className="p-4 text-gray-500">
                      {n.createdAt
                        ? new Date(n.createdAt).toLocaleDateString("en-PH", {
                            year: "numeric", month: "short", day: "numeric"
                          })
                        : "—"}
                    </td>
                    <td className="p-4">
                      {isAdmin && (
                        <button
                          onClick={() => setDeleteTarget(n)}
                          className="text-red-500 hover:text-red-700
                            text-xs hover:underline"
                        >
                          Remove
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Register Nurse modal */}
        {showForm && (
          <Modal title="Register New Nurse" onClose={() => setShowForm(false)}>
            <form onSubmit={handleSubmit} className="space-y-3">
              <FormField label="Full Name *"       field="fullName"
                form={form} setForm={setForm} required />
              <FormField label="Email *"           field="email"
                form={form} setForm={setForm} type="email" required />
              <FormField label="Phone Number *"    field="phoneNumber"
                form={form} setForm={setForm} required />
              <FormField label="Password *"        field="password"
                form={form} setForm={setForm} type="password" required />
              <FormField label="Confirm Password *" field="confirmPassword"
                form={form} setForm={setForm} type="password" required />

              {formError && (
                <p className="text-red-500 text-sm">{formError}</p>
              )}

              <div className="flex justify-end gap-2 pt-2">
                <button type="button" onClick={() => setShowForm(false)}
                  className="px-4 py-2 rounded-lg bg-gray-200 text-sm">
                  Cancel
                </button>
                <button type="submit" disabled={submitting}
                  className="px-4 py-2 rounded-lg bg-blue-600 text-white
                    text-sm disabled:opacity-60">
                  {submitting ? "Registering..." : "Register Nurse"}
                </button>
              </div>
            </form>
          </Modal>
        )}

        {/* Delete confirmation */}
        {deleteTarget && (
          <Modal title="Confirm Remove" onClose={() => setDeleteTarget(null)}>
            <p className="text-gray-600 mb-4">
              Remove nurse <strong>{deleteTarget.fullName}</strong> from the
              system? They will lose access immediately.
            </p>
            <div className="flex justify-end gap-2">
              <button onClick={() => setDeleteTarget(null)}
                className="px-4 py-2 rounded-lg bg-gray-200 text-sm">
                Cancel
              </button>
              <button onClick={handleDelete}
                className="px-4 py-2 rounded-lg bg-red-600 text-white text-sm">
                Remove Nurse
              </button>
            </div>
          </Modal>
        )}
      </main>
    </div>
  );
}

// ── Shared components ─────────────────────────────────────────────────────────

function FormField({ label, field, form, setForm,
                     type = "text", required = false }) {
  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">
        {label}
      </label>
      <input
        type={type}
        value={form[field]}
        onChange={e => setForm({ ...form, [field]: e.target.value })}
        required={required}
        className="border border-gray-300 rounded-lg p-2 w-full text-sm
          focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
    </div>
  );
}

function Modal({ title, onClose, children }) {
  return (
    <div className="fixed inset-0 bg-black bg-opacity-40 flex
      items-center justify-center z-50">
      <div className="bg-white rounded-xl shadow-xl w-full max-w-md mx-4
        max-h-[90vh] overflow-y-auto">
        <div className="flex justify-between items-center p-4 border-b">
          <h2 className="text-lg font-semibold">{title}</h2>
          <button onClick={onClose}
            className="text-gray-400 hover:text-gray-600 text-xl">
            ✕
          </button>
        </div>
        <div className="p-4">{children}</div>
      </div>
    </div>
  );
}