import { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import Layout from "../../widgets/layout/Layout";
import axios from "../../shared/api/axios";

export default function Profile() {
  const navigate = useNavigate();
  const [, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [form, setForm] = useState(null);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");

  const handleLogout = () => {
    localStorage.removeItem("meditrackUser");
    navigate("/login");
  };

  const loadProfile = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const res = await axios.get("/patient-portal/me");
      setProfile(res.data);
      setForm(res.data);
    } catch (e) {
      setError(e?.response?.data?.message || "Failed to load profile.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadProfile();
  }, [loadProfile]);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSave = async (e) => {
    e.preventDefault();
    if (!form) return;
    setSaving(true);
    setMessage("");
    try {
      const res = await axios.put("/patient-portal/me", form);
      setProfile(res.data);
      setForm(res.data);
      setMessage("Profile updated.");
      setTimeout(() => setMessage(""), 3000);
    } catch (e) {
      setMessage(e?.response?.data?.message || "Could not save profile.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <Layout>
      <div className="p-6 max-w-2xl mx-auto">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold">My Profile</h1>
          <button
            onClick={handleLogout}
            className="px-4 py-2 rounded-lg border border-red-300 text-red-600 text-sm hover:bg-red-50 focus:outline-none focus:ring-2 focus:ring-red-300"
          >
            Logout
          </button>
        </div>

        {loading && <p className="text-gray-500">Loading…</p>}
        {error && (
          <div role="alert"
            className="bg-red-50 border border-red-200 text-red-600
              rounded-lg p-3 mb-4 text-sm">{error}</div>
        )}

        {!loading && form && (
          <form onSubmit={handleSave} className="space-y-4">
            <div className="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">First name</label>
                  <p className="text-sm text-gray-900">{form.firstName}</p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Last name</label>
                  <p className="text-sm text-gray-900">{form.lastName}</p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Email</label>
                  <p className="text-sm text-gray-900">{form.email}</p>
                </div>

                <div>
                  <label htmlFor="phoneNumber" className="block text-sm font-medium text-gray-700">Phone number</label>
                  <input
                    id="phoneNumber"
                    name="phoneNumber"
                    value={form.phoneNumber || ""}
                    onChange={handleChange}
                    className="mt-1 border border-gray-300 rounded p-2 w-full text-sm
                      focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div>
                  <label htmlFor="gender" className="block text-sm font-medium text-gray-700">Gender</label>
                  <input
                    id="gender"
                    name="gender"
                    value={form.gender || ""}
                    onChange={handleChange}
                    className="mt-1 border border-gray-300 rounded p-2 w-full text-sm
                      focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div>
                  <label htmlFor="address" className="block text-sm font-medium text-gray-700">Address</label>
                  <input
                    id="address"
                    name="address"
                    value={form.address || ""}
                    onChange={handleChange}
                    className="mt-1 border border-gray-300 rounded p-2 w-full text-sm
                      focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div>
                  <label htmlFor="contactNumber" className="block text-sm font-medium text-gray-700">Contact number</label>
                  <input
                    id="contactNumber"
                    name="contactNumber"
                    value={form.contactNumber || ""}
                    onChange={handleChange}
                    className="mt-1 border border-gray-300 rounded p-2 w-full text-sm
                      focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div>
                  <label htmlFor="emergencyContact" className="block text-sm font-medium text-gray-700">Emergency contact</label>
                  <input
                    id="emergencyContact"
                    name="emergencyContact"
                    value={form.emergencyContact || ""}
                    onChange={handleChange}
                    className="mt-1 border border-gray-300 rounded p-2 w-full text-sm
                      focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                {"birthDate" in form && (
                  <div>
                    <label htmlFor="birthDate" className="block text-sm font-medium text-gray-700">Birth date</label>
                    <input
                      id="birthDate"
                      type="date"
                      name="birthDate"
                      value={form.birthDate || ""}
                      onChange={handleChange}
                      className="mt-1 border border-gray-300 rounded p-2 w-full text-sm
                        focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                )}
              </div>
            </div>

            <div className="flex justify-end gap-3">
              <button
                type="submit"
                disabled={saving}
                className="px-4 py-2 rounded-lg bg-blue-600 text-white text-sm
                  disabled:opacity-60 focus:outline-none focus:ring-2
                  focus:ring-blue-300 focus:ring-offset-1"
              >
                {saving ? "Saving…" : "Save changes"}
              </button>
            </div>

            {message && (
              <p role="status" aria-live="polite"
                className="text-sm text-green-700">{message}</p>
            )}
          </form>
        )}
      </div>
    </Layout>
  );
}

