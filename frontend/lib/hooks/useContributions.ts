'use client';

import { useState, useEffect } from 'react';
import { apiGet, apiPost } from '../api/client';
import { Contribution, ContributionCreate, ContributionStatus } from '../types';

export function useContributions(status?: ContributionStatus) {
  const [contributions, setContributions] = useState<Contribution[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchContributions = async () => {
    setLoading(true);
    setError(null);
    try {
      const params = status ? `?status=${status}` : '';
      const data = await apiGet<Contribution[]>(`/contributions${params}`);
      setContributions(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch contributions');
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchContributions();
  }, [status]);

  const submitContribution = async (contribution: ContributionCreate): Promise<void> => {
    const newContribution = await apiPost<Contribution>('/contributions', contribution);
    setContributions(prev => [newContribution, ...prev]);
  };

  const approveContribution = async (id: number): Promise<void> => {
    await apiPost(`/contributions/${id}/approve`);
    await fetchContributions();
  };

  const rejectContribution = async (id: number, reason?: string): Promise<void> => {
    await apiPost(`/contributions/${id}/reject`, { status: 'rejected', reason });
    await fetchContributions();
  };

  return {
    contributions,
    loading,
    error,
    submitContribution,
    approveContribution,
    rejectContribution,
    refetch: fetchContributions,
  };
}