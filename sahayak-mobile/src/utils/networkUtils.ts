/**
 * Network Utilities for Poor Connectivity
 * Retry logic, timeout handling, and offline caching for rural deployment
 */

import NetInfo from '@react-native-community/netinfo';

export interface FetchOptions extends RequestInit {
  /** Timeout in milliseconds (default: 30000 for slow networks) */
  timeout?: number;
  /** Number of retry attempts (default: 3) */
  retries?: number;
  /** Base delay between retries in ms (default: 1000) */
  retryDelay?: number;
  /** Callback when retry occurs */
  onRetry?: (attempt: number, error: Error) => void;
}

export interface FetchResult<T> {
  data: T | null;
  error: Error | null;
  isOffline: boolean;
  attempts: number;
}

/**
 * Check if device is online
 */
export async function isOnline(): Promise<boolean> {
  try {
    const state = await NetInfo.fetch();
    return state.isConnected === true && state.isInternetReachable !== false;
  } catch {
    return true; // Assume online if we can't check
  }
}

/**
 * Fetch with timeout support
 */
async function fetchWithTimeout(
  url: string,
  options: RequestInit = {},
  timeout: number = 30000
): Promise<Response> {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeout);

  try {
    const response = await fetch(url, {
      ...options,
      signal: controller.signal,
    });
    return response;
  } finally {
    clearTimeout(timeoutId);
  }
}

/**
 * Calculate exponential backoff delay
 */
function getBackoffDelay(attempt: number, baseDelay: number): number {
  // Exponential backoff with jitter: delay * 2^attempt + random jitter
  const exponentialDelay = baseDelay * Math.pow(2, attempt);
  const jitter = Math.random() * 1000;
  return Math.min(exponentialDelay + jitter, 30000); // Max 30 seconds
}

/**
 * Resilient fetch with retry logic for poor connectivity
 * Designed for rural areas with intermittent network
 */
export async function resilientFetch<T = any>(
  url: string,
  options: FetchOptions = {}
): Promise<FetchResult<T>> {
  const {
    timeout = 30000,
    retries = 3,
    retryDelay = 1000,
    onRetry,
    ...fetchOptions
  } = options;

  let lastError: Error | null = null;
  let attempts = 0;

  // Check if offline before attempting
  const online = await isOnline();
  if (!online) {
    return {
      data: null,
      error: new Error('No internet connection'),
      isOffline: true,
      attempts: 0,
    };
  }

  for (let attempt = 0; attempt <= retries; attempt++) {
    attempts = attempt + 1;
    
    try {
      const response = await fetchWithTimeout(url, fetchOptions, timeout);
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      const data = await response.json();
      return {
        data,
        error: null,
        isOffline: false,
        attempts,
      };
    } catch (error) {
      lastError = error instanceof Error ? error : new Error(String(error));
      
      // Check if it's an abort error (timeout)
      if (lastError.name === 'AbortError') {
        lastError = new Error('Request timed out. Please check your connection.');
      }
      
      // Don't retry on certain errors
      if (
        lastError.message.includes('HTTP 4') || // Client errors
        attempt === retries // Last attempt
      ) {
        break;
      }
      
      // Notify about retry
      onRetry?.(attempt + 1, lastError);
      
      // Wait before retrying
      const delay = getBackoffDelay(attempt, retryDelay);
      await new Promise((resolve) => setTimeout(resolve, delay));
      
      // Check if still online before retrying
      const stillOnline = await isOnline();
      if (!stillOnline) {
        return {
          data: null,
          error: new Error('Lost internet connection'),
          isOffline: true,
          attempts,
        };
      }
    }
  }

  return {
    data: null,
    error: lastError,
    isOffline: false,
    attempts,
  };
}

/**
 * Wrapper for FormData uploads with progress and retry
 */
export async function resilientUpload<T = any>(
  url: string,
  formData: FormData,
  options: Omit<FetchOptions, 'body'> = {}
): Promise<FetchResult<T>> {
  return resilientFetch<T>(url, {
    method: 'POST',
    body: formData,
    // Don't set Content-Type for FormData - browser handles it
    ...options,
    timeout: options.timeout ?? 60000, // Longer timeout for uploads
  });
}

/**
 * User-friendly error messages for common network errors
 */
export function getNetworkErrorMessage(error: Error, language: string = 'en-IN'): string {
  const messages: Record<string, Record<string, string>> = {
    'en-IN': {
      timeout: 'Request timed out. Please check your internet connection and try again.',
      offline: 'No internet connection. Please check your network settings.',
      server: 'Server is not responding. Please try again later.',
      unknown: 'Something went wrong. Please try again.',
    },
    'hi-IN': {
      timeout: 'अनुरोध का समय समाप्त हो गया। कृपया अपना इंटरनेट कनेक्शन जांचें और पुनः प्रयास करें।',
      offline: 'इंटरनेट कनेक्शन नहीं है। कृपया अपनी नेटवर्क सेटिंग्स जांचें।',
      server: 'सर्वर जवाब नहीं दे रहा है। कृपया बाद में पुनः प्रयास करें।',
      unknown: 'कुछ गड़बड़ हुई। कृपया पुनः प्रयास करें।',
    },
    'ta-IN': {
      timeout: 'கோரிக்கை நேரம் முடிந்தது. உங்கள் இணைய இணைப்பைச் சரிபார்த்து மீண்டும் முயற்சிக்கவும்.',
      offline: 'இணைய இணைப்பு இல்லை. உங்கள் நெட்வொர்க் அமைப்புகளைச் சரிபார்க்கவும்.',
      server: 'சர்வர் பதிலளிக்கவில்லை. பின்னர் மீண்டும் முயற்சிக்கவும்.',
      unknown: 'ஏதோ தவறு நடந்தது. மீண்டும் முயற்சிக்கவும்.',
    },
  };

  const errorType = error.message.includes('timeout') || error.message.includes('timed out')
    ? 'timeout'
    : error.message.includes('connection') || error.message.includes('network')
    ? 'offline'
    : error.message.includes('server') || error.message.includes('HTTP 5')
    ? 'server'
    : 'unknown';

  const langMessages = messages[language] || messages['en-IN'];
  return langMessages[errorType] || langMessages.unknown;
}

export default resilientFetch;
