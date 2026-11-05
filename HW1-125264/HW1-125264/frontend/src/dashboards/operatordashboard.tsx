import React, { useCallback, useEffect, useState } from 'react';

const API = 'http://localhost:8080/api/v1/operator';

const PARCEL_STATES = [
  'CREATED',
  'IN_TRANSIT',
  'READY_FOR_PICKUP',
  'PICKED_UP',
  'EXPIRED',
  'RETURNED',
  'CANCELLED',
  'DESTROYED',
];

interface Station {
  id: number;
  name: string;
  addr: string;
}

interface Parcel {
  trackingCode: string;
  station: string;
  size: string;
  state: string;
  recipientContact: string;
  pickupDeadline: string | null;
}

interface Compartment {
  id: number;
  size: string;
  status: string;
}

const stateBadgeClass = (state: string) => {
  if (state === 'EXPIRED') return 'bg-red-100 text-red-800';
  if (state === 'RETURNED' || state === 'CANCELLED' || state === 'DESTROYED') {
    return 'bg-gray-100 text-gray-800';
  }
  if (state === 'PICKED_UP') return 'bg-green-100 text-green-800';
  return 'bg-blue-100 text-blue-800';
};

const compartmentBadgeClass = (status: string) => {
  if (status === 'FAULTY') return 'bg-red-100 text-red-800';
  if (status === 'OCCUPIED') return 'bg-yellow-100 text-yellow-800';
  return 'bg-green-100 text-green-800';
};

export const OperatorDashboard: React.FC = () => {
  const [stations, setStations] = useState<Station[]>([]);
  const [selectedStation, setSelectedStation] = useState('');
  const [selectedState, setSelectedState] = useState('ALL');
  const [parcels, setParcels] = useState<Parcel[]>([]);
  const [compartments, setCompartments] = useState<Compartment[]>([]);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const selectedStationObj = stations.find((s) => s.name === selectedStation);

  const loadStations = useCallback(async () => {
    try {
      const response = await fetch(`${API}/stations`);
      if (!response.ok) throw new Error('Failed to load stations');
      const data: Station[] = await response.json();
      setStations(data);
      setSelectedStation((current) => current || data[0]?.name || '');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load stations');
    }
  }, []);

  const loadParcels = useCallback(async () => {
    if (!selectedStation) {
      setParcels([]);
      return;
    }

    const params = new URLSearchParams({ station: selectedStation });
    if (selectedState !== 'ALL') params.set('state', selectedState);

    try {
      const response = await fetch(`${API}/parcels?${params.toString()}`);
      if (!response.ok) throw new Error('Failed to load parcels');
      setParcels(await response.json());
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load parcels');
    }
  }, [selectedStation, selectedState]);

  const loadCompartments = useCallback(async () => {
    if (!selectedStationObj) {
      setCompartments([]);
      return;
    }

    try {
      const response = await fetch(`${API}/stations/${selectedStationObj.id}/compartments`);
      if (!response.ok) throw new Error('Failed to load compartments');
      setCompartments(await response.json());
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load compartments');
    }
  }, [selectedStationObj]);

  useEffect(() => {
    loadStations();
  }, [loadStations]);

  useEffect(() => {
    loadParcels();
  }, [loadParcels]);

  useEffect(() => {
    loadCompartments();
  }, [loadCompartments]);

  const refresh = async () => {
    await Promise.all([loadParcels(), loadCompartments()]);
  };

  const handleMarkReturned = async (trackingCode: string) => {
    setError(null);
    setMessage(null);
    try {
      const response = await fetch(`${API}/parcels/${trackingCode}/return`, { method: 'PUT' });
      if (!response.ok) {
        const body = await response.json().catch(() => ({}));
        throw new Error(body.error || 'Failed to register removal');
      }
      setMessage(`Parcel ${trackingCode} registered as removed.`);
      await refresh();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to register removal');
    }
  };

  const handleOutOfService = async (compartmentId: number) => {
    setError(null);
    setMessage(null);
    try {
      const response = await fetch(`${API}/compartments/${compartmentId}/out-of-service`, {
        method: 'PUT',
      });
      if (!response.ok) {
        const body = await response.json().catch(() => ({}));
        throw new Error(body.error || 'Failed to put compartment out of service');
      }
      setMessage(`Compartment ${compartmentId} put out of service.`);
      await loadCompartments();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to put compartment out of service');
    }
  };

  return (
    <div className="max-w-6xl mx-auto p-6 space-y-6">
      <h1 className="text-3xl font-bold text-gray-800">Operator Dashboard</h1>

      <div className="p-4 bg-white border border-gray-200 rounded-lg shadow-sm flex flex-col sm:flex-row gap-4">
        <label className="flex flex-col gap-1 flex-1">
          <span className="text-sm font-medium text-gray-700">Station</span>
          <select
            value={selectedStation}
            onChange={(e) => setSelectedStation(e.target.value)}
            className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            {stations.length === 0 && <option value="">No stations</option>}
            {stations.map((station) => (
              <option key={station.id} value={station.name}>
                {station.name} — {station.addr}
              </option>
            ))}
          </select>
        </label>

        <label className="flex flex-col gap-1 flex-1">
          <span className="text-sm font-medium text-gray-700">Parcel state</span>
          <select
            value={selectedState}
            onChange={(e) => setSelectedState(e.target.value)}
            className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="ALL">All states</option>
            {PARCEL_STATES.map((state) => (
              <option key={state} value={state}>
                {state}
              </option>
            ))}
          </select>
        </label>
      </div>

      {message && (
        <div className="p-4 bg-green-50 border border-green-200 rounded-lg text-green-800 font-medium">
          {message}
        </div>
      )}

      {error && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-800 font-medium">
          {error}
        </div>
      )}

      <div className="space-y-4">
        <h2 className="text-xl font-semibold text-gray-700">Parcels</h2>

        <div className="overflow-x-auto border border-gray-200 rounded-lg shadow-sm" tabIndex={0}>
          <table className="min-w-full divide-y divide-gray-200 bg-white text-sm text-left">
            <thead className="bg-gray-50 text-gray-700 font-semibold uppercase tracking-wider">
              <tr>
                <th className="px-6 py-3">Tracking Code</th>
                <th className="px-6 py-3">Size</th>
                <th className="px-6 py-3">State</th>
                <th className="px-6 py-3">Recipient</th>
                <th className="px-6 py-3">Pickup Deadline</th>
                <th className="px-6 py-3">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {parcels.map((parcel) => (
                <tr
                  key={parcel.trackingCode}
                  className={`transition-colors ${
                    parcel.state === 'EXPIRED' ? 'bg-red-50' : 'hover:bg-gray-50'
                  }`}
                >
                  <td className="px-6 py-4 font-mono font-medium text-gray-900">
                    {parcel.trackingCode}
                  </td>
                  <td className="px-6 py-4 text-gray-700">{parcel.size}</td>
                  <td className="px-6 py-4">
                    <span
                      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold ${stateBadgeClass(
                        parcel.state,
                      )}`}
                    >
                      {parcel.state}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-gray-700">{parcel.recipientContact}</td>
                  <td className="px-6 py-4 text-gray-700">
                    {parcel.pickupDeadline ?? '—'}
                  </td>
                  <td className="px-6 py-4">
                    {parcel.state === 'EXPIRED' && (
                      <button
                        onClick={() => handleMarkReturned(parcel.trackingCode)}
                        className="px-3 py-1.5 bg-red-600 hover:bg-red-700 text-white text-xs font-medium rounded transition-colors"
                      >
                        Register removal
                      </button>
                    )}
                  </td>
                </tr>
              ))}
              {parcels.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-6 py-4 text-center text-gray-500">
                    No parcels found for this station and state.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <div className="space-y-4">
        <h2 className="text-xl font-semibold text-gray-700">Compartments</h2>

        <div className="overflow-x-auto border border-gray-200 rounded-lg shadow-sm">
          <table className="min-w-full divide-y divide-gray-200 bg-white text-sm text-left">
            <thead className="bg-gray-50 text-gray-700 font-semibold uppercase tracking-wider">
              <tr>
                <th className="px-6 py-3">Compartment ID</th>
                <th className="px-6 py-3">Size</th>
                <th className="px-6 py-3">Status</th>
                <th className="px-6 py-3">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {compartments.map((compartment) => (
                <tr key={compartment.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 font-medium text-gray-900">{compartment.id}</td>
                  <td className="px-6 py-4 text-gray-700">{compartment.size}</td>
                  <td className="px-6 py-4">
                    <span
                      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold ${compartmentBadgeClass(
                        compartment.status,
                      )}`}
                    >
                      {compartment.status}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <button
                      onClick={() => handleOutOfService(compartment.id)}
                      disabled={compartment.status !== 'AVAILABLE'}
                      className="px-3 py-1.5 bg-red-600 hover:bg-red-700 text-white text-xs font-medium rounded transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
                    >
                      Put out of service
                    </button>
                  </td>
                </tr>
              ))}
              {compartments.length === 0 && (
                <tr>
                  <td colSpan={4} className="px-6 py-4 text-center text-gray-500">
                    No compartments found for this station.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
