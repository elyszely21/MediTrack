import { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';
import Input from '../components/Input';
import Button from '../components/Button';
import api from '../api/axios';

const emptyBillForm = { description: '', amount: '', billDate: '' };
const emptyPaymentForm = { amountPaid: '', paymentDate: '', paymentMethod: '', receivedBy: '' };

const Billing = () => {
  const [patients, setPatients] = useState([]);
  const [patientId, setPatientId] = useState('');
  const [bills, setBills] = useState([]);
  const [billForm, setBillForm] = useState(emptyBillForm);
  const [showBillForm, setShowBillForm] = useState(false);
  const [activeBillId, setActiveBillId] = useState(null);
  const [payments, setPayments] = useState([]);
  const [paymentForm, setPaymentForm] = useState(emptyPaymentForm);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/patients').then((res) => setPatients(res.data)).catch(() => {});
  }, []);

  const loadBills = async (id) => {
    if (!id) { setBills([]); return; }
    try {
      const res = await api.get(`/bills/patient/${id}`);
      setBills(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load bills');
    }
  };

  useEffect(() => { loadBills(patientId); setActiveBillId(null); setPayments([]); }, [patientId]);

  const handleBillChange = (e) => setBillForm({ ...billForm, [e.target.name]: e.target.value });

  const handleCreateBill = async (e) => {
    e.preventDefault();
    setError('');
    if (!patientId) { setError('Select a patient first'); return; }
    try {
      await api.post('/bills', { ...billForm, patientId: Number(patientId), amount: Number(billForm.amount) });
      setBillForm(emptyBillForm);
      setShowBillForm(false);
      loadBills(patientId);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create bill');
    }
  };

  const handleDeleteBill = async (id) => {
    if (!window.confirm('Delete this bill?')) return;
    try {
      await api.delete(`/bills/${id}`);
      loadBills(patientId);
      if (activeBillId === id) { setActiveBillId(null); setPayments([]); }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete bill');
    }
  };

  const openPayments = async (billId) => {
    setActiveBillId(billId);
    setError('');
    try {
      const res = await api.get(`/bills/${billId}/payments`);
      setPayments(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load payments');
    }
  };

  const handlePaymentChange = (e) => setPaymentForm({ ...paymentForm, [e.target.name]: e.target.value });

  const handleRecordPayment = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await api.post(`/bills/${activeBillId}/payments`, {
        ...paymentForm,
        amountPaid: Number(paymentForm.amountPaid),
      });
      setPaymentForm(emptyPaymentForm);
      openPayments(activeBillId);
      loadBills(patientId);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to record payment');
    }
  };

  const handlePrintReceipt = (payment, bill) => {
    const patient = patients.find((p) => p.id === Number(patientId));
    const win = window.open('', '_blank');
    win.document.write(`
      <html><head><title>Receipt</title>
      <style>body{font-family:sans-serif;padding:40px;} .line{margin:8px 0;}</style>
      </head><body>
      <h2>MediTrack Clinic</h2>
      <p>Official Receipt</p><hr/>
      <div class="line"><b>Patient:</b> ${patient ? patient.firstName + ' ' + patient.lastName : ''}</div>
      <div class="line"><b>Bill:</b> ${bill?.description || ''}</div>
      <div class="line"><b>Date:</b> ${payment.paymentDate}</div>
      <div class="line"><b>Amount Paid:</b> ${Number(payment.amountPaid).toFixed(2)}</div>
      <div class="line"><b>Method:</b> ${payment.paymentMethod || '-'}</div>
      <div class="line"><b>Received By:</b> ${payment.receivedBy}</div>
      </body></html>
    `);
    win.document.close();
    win.print();
  };

  const activeBill = bills.find((b) => b.id === activeBillId);

  return (
    <div className="dashboard-page">
      <Navbar />
      <main className="dashboard-content">
        <h1>Billing</h1>

        <div className="form-group">
          <label htmlFor="patientSelect">Patient</label>
          <select id="patientSelect" className="input-field" value={patientId} onChange={(e) => setPatientId(e.target.value)}>
            <option value="">Select patient</option>
            {patients.map((p) => (
              <option key={p.id} value={p.id}>{p.firstName} {p.lastName}</option>
            ))}
          </select>
        </div>

        {error && <p style={{ color: 'red' }}>{error}</p>}

        {patientId && (
          <>
            <Button label={showBillForm ? 'Cancel' : 'New Bill'} onClick={() => setShowBillForm(!showBillForm)} />

            {showBillForm && (
              <form onSubmit={handleCreateBill} style={{ margin: '16px 0' }}>
                <Input label="Description" name="description" value={billForm.description} onChange={handleBillChange} />
                <Input label="Amount" type="number" name="amount" value={billForm.amount} onChange={handleBillChange} />
                <Input label="Bill Date" type="date" name="billDate" value={billForm.billDate} onChange={handleBillChange} />
                <Button label="Save Bill" type="submit" />
              </form>
            )}

            <table className="data-table">
              <thead>
                <tr><th>Date</th><th>Description</th><th>Amount</th><th>Balance</th><th>Status</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {bills.map((b) => (
                  <tr key={b.id}>
                    <td>{b.billDate}</td>
                    <td>{b.description}</td>
                    <td>{Number(b.amount).toFixed(2)}</td>
                    <td>{Number(b.balance).toFixed(2)}</td>
                    <td>{b.status}</td>
                    <td>
                      <button className="action-button" onClick={() => openPayments(b.id)}>Payments</button>{' '}
                      <button className="action-button" onClick={() => handleDeleteBill(b.id)}>Delete</button>
                    </td>
                  </tr>
                ))}
                {bills.length === 0 && <tr><td colSpan="6">No bills yet.</td></tr>}
              </tbody>
            </table>

            {activeBillId && (
              <div style={{ marginTop: 24 }}>
                <h3>Payment History — {activeBill?.description}</h3>
                {activeBill?.status !== 'PAID' && (
                  <form onSubmit={handleRecordPayment} style={{ margin: '12px 0' }}>
                    <Input label={`Amount Paid (balance: ${Number(activeBill?.balance ?? 0).toFixed(2)})`} type="number" name="amountPaid" value={paymentForm.amountPaid} onChange={handlePaymentChange} />
                    <Input label="Payment Date" type="date" name="paymentDate" value={paymentForm.paymentDate} onChange={handlePaymentChange} />
                    <Input label="Payment Method" name="paymentMethod" value={paymentForm.paymentMethod} onChange={handlePaymentChange} placeholder="Cash, GCash, etc." />
                    <Input label="Received By" name="receivedBy" value={paymentForm.receivedBy} onChange={handlePaymentChange} />
                    <Button label="Record Payment" type="submit" />
                  </form>
                )}

                <table className="data-table">
                  <thead>
                    <tr><th>Date</th><th>Amount</th><th>Method</th><th>Received By</th><th>Actions</th></tr>
                  </thead>
                  <tbody>
                    {payments.map((p) => (
                      <tr key={p.id}>
                        <td>{p.paymentDate}</td>
                        <td>{Number(p.amountPaid).toFixed(2)}</td>
                        <td>{p.paymentMethod}</td>
                        <td>{p.receivedBy}</td>
                        <td><button className="action-button" onClick={() => handlePrintReceipt(p, activeBill)}>Print Receipt</button></td>
                      </tr>
                    ))}
                    {payments.length === 0 && <tr><td colSpan="5">No payments recorded.</td></tr>}
                  </tbody>
                </table>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
};

export default Billing;