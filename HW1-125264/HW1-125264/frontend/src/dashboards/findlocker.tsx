import React, { useState } from 'react';

interface Station {
  id?: string;
  name: string;
  addr: string;
  latitude: number;
  longitude: number;
  freeCompartmentsBySize?: Record<string, number>;
}

export const FindLocker: React.FC = () => {
  const [address, setAddress] = useState('');
  const [stations, setStations] = useState<Station[] | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      const response = await fetch(`http://localhost:8080/api/v1/stations?addr=${address}`);
      if (response.ok) {
        const data = await response.json();
        setStations(data);
      }
    } catch (error) {
      console.error('Failed to fetch stations:', error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto p-6 space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Find Nearest Locker</h1>

      <form onSubmit={handleSubmit} className="flex gap-3">
        <input
          type="text"
          name="address"
          value={address}
          onChange={(e) => setAddress(e.target.value)}
          placeholder="Enter your address"
          className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <button
          type="submit"
          disabled={isLoading}
          className="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors disabled:opacity-50"
        >
          {isLoading ? 'Searching...' : 'Search'}
        </button>
      </form>

      {stations && (
        <div className="space-y-4">
          {stations.map((s, index) => (
            <div key={s.id || index} className="p-4 bg-white border border-gray-200 rounded-lg shadow-sm">
              <h3 className="text-lg font-semibold text-gray-900">{s.name}</h3>
              <p className="text-gray-600 mt-1">{s.addr}</p>
              {s.freeCompartmentsBySize && (
                <div className="mt-3 flex flex-wrap gap-2">
                  {(['S', 'M', 'L'] as const).map((size) => (
                    <span
                      key={size}
                      className="px-3 py-1 bg-gray-100 text-sm font-medium text-gray-700 rounded-full"
                    >
                      {size}: {s.freeCompartmentsBySize?.[size] ?? 0} free
                    </span>
                  ))}
                </div>
              )}
            </div>
          ))}
          {stations.length === 0 && (
            <p className="text-gray-500">No stations found for this address.</p>
          )}
        </div>
      )}
    </div>
  );
};
