import React, { useState } from 'react';

interface StateHistory {
    state: string;
    timestamp: string;
    actor: string | null;
}

interface ParcelTrackDetails {
    trackingCode: string;
    pickupCode: string;
    station: string;
    recipientContact: string;
    size: string;
    state: string;
    createdAt: string;
    pickupDeadline: string;
    readyForPickupAt: string | null;

    // Add the history list here
    history: StateHistory[];
}

export const TrackParcel: React.FC = () => {
  const [trackingCode, setTrackingCode] = useState('');
  const [parcel, setParcel] = useState<ParcelTrackDetails | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      const response = await fetch(`http://localhost:8080/api/v1/parcels/${trackingCode}`);
      if (response.ok) {
        const data = await response.json();
        setParcel(data);
      }
    } catch (error) {
      console.error('Failed to track parcel:', error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto p-6 space-y-6">
      <h1 className="text-2xl font-bold text-gray-800">Track Your Parcel</h1>

      <form onSubmit={handleSubmit} className="flex gap-3">
        <input
          type="text"
          name="trackingCode"
          value={trackingCode}
          onChange={(e) => setTrackingCode(e.target.value)}
          placeholder="Tracking code"
          className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <button
          type="submit"
          disabled={isLoading}
          className="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors disabled:opacity-50"
        >
          {isLoading ? 'Tracking...' : 'Track'}
        </button>
      </form>

      {parcel && (
        <div className="p-6 bg-white border border-gray-200 rounded-lg shadow-sm space-y-3">
          <p className="text-gray-700">
            Status: <strong className="font-semibold text-blue-600">{parcel.state}</strong>
          </p>
          <p className="text-gray-700">
            Station: <span className="text-gray-900">{parcel.station}</span>
          </p>
          <p className="text-gray-700">
            Pickup deadline: <span className="text-gray-900">{parcel.pickupDeadline}</span>
          </p>
            {parcel.pickupCode ? (
                <p>
                    Pickup Code: <span className='text-gray-900'>{parcel.pickupCode}</span>
                </p>
                ) : (
                <p></p>
                )
            }
          <div className="mt-4 pt-4 border-t border-gray-200">
            <p className="text-gray-700 font-semibold mb-3">History</p>

            {parcel.history && parcel.history.length > 0 ? (
            <ul className="space-y-3">
                {parcel.history.map((transition, index) => (
                <li key={index} className="flex flex-col p-3 bg-gray-50 rounded-md border border-gray-100">
                    <span className="font-medium text-sm text-gray-800">
                    {transition.state}
                    </span>
                    <span className="text-xs text-gray-500 mt-1">
                    {new Date(transition.timestamp).toLocaleString()}
                    </span>
                    {transition.actor && (
                    <span className="text-xs text-gray-500 mt-0.5">
                        User: {transition.actor}
                    </span>
                    )}
                </li>
                ))}
            </ul>
            ) : (
            <p className="text-sm text-gray-500 italic">No history available.</p>
            )}
            </div>

        </div>
      )}
    </div>
  );
};
