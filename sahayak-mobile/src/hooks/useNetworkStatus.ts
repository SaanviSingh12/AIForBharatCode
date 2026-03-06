/**
 * Network Status Hook
 * Monitors network connectivity for offline handling in rural areas
 */

import { useState, useEffect, useCallback } from 'react';
import NetInfo, { NetInfoState, NetInfoStateType } from '@react-native-community/netinfo';

export interface NetworkStatus {
  isConnected: boolean;
  isInternetReachable: boolean | null;
  connectionType: NetInfoStateType;
  isSlowConnection: boolean;
  details: {
    isConnectionExpensive?: boolean;
    cellularGeneration?: string | null;
  };
}

export interface UseNetworkStatusReturn {
  networkStatus: NetworkStatus;
  isOnline: boolean;
  isOffline: boolean;
  isSlowNetwork: boolean;
  refresh: () => Promise<void>;
}

/**
 * Hook to monitor network connectivity
 * Designed for rural deployment with poor connectivity detection
 */
export function useNetworkStatus(): UseNetworkStatusReturn {
  const [networkStatus, setNetworkStatus] = useState<NetworkStatus>({
    isConnected: true,
    isInternetReachable: true,
    connectionType: NetInfoStateType.unknown,
    isSlowConnection: false,
    details: {},
  });

  const processNetworkState = useCallback((state: NetInfoState): NetworkStatus => {
    const isConnected = state.isConnected ?? false;
    const isInternetReachable = state.isInternetReachable;
    
    // Determine if connection is slow (2G or no cellular generation info)
    let isSlowConnection = false;
    let cellularGeneration: string | null = null;
    
    if (state.type === 'cellular' && state.details) {
      cellularGeneration = state.details.cellularGeneration ?? null;
      // 2G networks are considered slow
      isSlowConnection = cellularGeneration === '2g' || cellularGeneration === null;
    }
    
    return {
      isConnected,
      isInternetReachable,
      connectionType: state.type,
      isSlowConnection,
      details: {
        isConnectionExpensive: state.details?.isConnectionExpensive,
        cellularGeneration,
      },
    };
  }, []);

  const refresh = useCallback(async () => {
    try {
      const state = await NetInfo.fetch();
      setNetworkStatus(processNetworkState(state));
    } catch (error) {
      console.error('Failed to fetch network state:', error);
    }
  }, [processNetworkState]);

  useEffect(() => {
    // Initial fetch
    refresh();

    // Subscribe to network state changes
    const unsubscribe = NetInfo.addEventListener((state) => {
      setNetworkStatus(processNetworkState(state));
    });

    return () => {
      unsubscribe();
    };
  }, [processNetworkState, refresh]);

  return {
    networkStatus,
    isOnline: networkStatus.isConnected && networkStatus.isInternetReachable !== false,
    isOffline: !networkStatus.isConnected || networkStatus.isInternetReachable === false,
    isSlowNetwork: networkStatus.isSlowConnection,
    refresh,
  };
}

export default useNetworkStatus;
