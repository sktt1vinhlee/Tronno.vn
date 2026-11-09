import api from "./client";

export const debtApi = {
  list: (status) => api.get("/debts", { params: status ? { status } : {} }),
  get: (id) => api.get(`/debts/${id}`),
  create: (payload) => api.post("/debts", payload),
  update: (id, payload) => api.put(`/debts/${id}`, payload),
  changeStatus: (id, status) => api.patch(`/debts/${id}/status`, null, { params: { status } }),
  remove: (id) => api.delete(`/debts/${id}`),
  schedule: (id) => api.get(`/debts/${id}/schedule`),
  payments: (id) => api.get(`/debts/${id}/payments`),
  pay: (id, payload) => api.post(`/debts/${id}/payments`, payload),
  reversePayment: (paymentId, reason) =>
    api.post(`/debts/payments/${paymentId}/reverse`, { reason }),
  rateHistory: (id) => api.get(`/debts/${id}/rate-history`),
  changeRate: (id, payload) => api.post(`/debts/${id}/rate-history`, payload),
  extend: (id, payload) => api.post(`/debts/${id}/extend`, payload),
};

export const creditorApi = {
  list: () => api.get("/creditors"),
  create: (payload) => api.post("/creditors", payload),
  update: (id, payload) => api.put(`/creditors/${id}`, payload),
  remove: (id) => api.delete(`/creditors/${id}`),
};

export const reportApi = {
  dashboard: () => api.get("/dashboard"),
};

export const reminderApi = {
  list: () => api.get("/reminders"),
  listByDebt: (debtId) => api.get(`/debts/${debtId}/reminders`),
  sendNow: (id) => api.post(`/reminders/${id}/send`),
};

export const letterApi = {
  compose: (payload) => api.post("/letters/compose", payload),
  rateCheck: (payload) => api.post("/letters/rate-check", payload),
};
