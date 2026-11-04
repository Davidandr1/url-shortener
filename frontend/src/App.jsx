import React, { useState, useEffect } from 'react';

const API = 'http://localhost:8080';

export default function App() {
  const [activeTab, setActiveTab] = useState('shorten');
  const [authMode, setAuthMode] = useState('login'); // 'login' or 'register'
  const [token, setToken] = useState(localStorage.getItem('jwt') || '');

  // Form states
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [originalUrl, setOriginalUrl] = useState('');

  // Feedback states
  const [authMessage, setAuthMessage] = useState({ text: '', isError: false });
  const [shortenResult, setShortenResult] = useState(null);
  const [userLinks, setUserLinks] = useState([]);

  // Save token to localStorage when it changes
  useEffect(() => {
    if (token) {
      localStorage.setItem('jwt', token);
    } else {
      localStorage.removeItem('jwt');
    }
  }, [token]);

  // Handle Login / Register
  const handleAuthSubmit = async (e) => {
    e.preventDefault();
    setAuthMessage({ text: '', isError: false });

    const endpoint =
        authMode === 'register'
            ? `${API}/user/register?email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`
            : `${API}/user/login`;

    const options =
        authMode === 'login'
            ? {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ email, password }),
            }
            : { method: 'POST' };

    try {
      const response = await fetch(endpoint, options);

      if (authMode === 'register') {
        const text = await response.text();
        setAuthMessage({ text, isError: !response.ok });
      } else if(!response.ok) {
        setAuthMessage({text: 'Login failed. Check your credentials.', isError: true});
      }else{
        const data = await response.json();

        if (response.ok && data.token) {
          setToken(data.token);
          setAuthMessage({ text: 'Logged in successfully!', isError: false });
          setActiveTab('shorten');
        } else {
          setAuthMessage({ text: 'Login failed. Check your credentials.', isError: true });
        }
      }
    } catch (err) {
      setAuthMessage({ text: 'Error connecting to the backend server.', isError: true });
    }
  };

  const fetchUserLinks = async () => {
    try {
      const response = await fetch(`${API}/links`, {
        method: 'GET',
        headers: { Authorization: `Bearer ${token}` },
      });
      if (response.ok) {
        setUserLinks(await response.json());
      }
    } catch (err) {
      console.error('Failed to fetch Links', err);
    }
  };

  // Handle URL Shortening (Protected Endpoint)
  const handleShortenSubmit = async (e) => {
    e.preventDefault();
    setShortenResult(null);

    try {
      const response = await fetch(`${API}/links`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ url: originalUrl }),
      });

      if (response.ok) {
        setShortenResult(await response.json());
      } else if (response.status === 401) {
        alert('Session expired or unauthorized. Please log in again.');
        setToken('');
        setActiveTab('auth');
      } else {
        alert('Failed to shorten URL.');
      }
    } catch (err) {
      console.error(err);
      alert('Error connecting to backend.');
    }
  };

  const handleLogout = () => {
    setToken('');
    setUserLinks([]);
    setShortenResult(null);
    setActiveTab('shorten');
  };

  const navBtn = (tab) =>
      `px-3 py-2 rounded-lg font-medium transition ${
          activeTab === tab
              ? 'bg-white/25 text-white'
              : 'text-indigo-100 hover:bg-white/15 hover:text-white'
      }`;

  const inputClass =
      'mt-1 block w-full px-3 py-2 text-center border border-gray-300 rounded-lg shadow-sm focus:ring-indigo-500 focus:border-indigo-500 focus:outline-none';

  return (
      <div className="bg-gray-50 text-gray-800 font-sans antialiased min-h-screen w-full">
        {/* Banner */}
        <header className="sticky top-0 z-10 w-full bg-gray-500">
          <div className="max-w-6xl mx-auto px-6 py-3 flex items-center justify-between">
            {/* Left: logo + Home + Links */}
            <div className="flex items-center gap-2">
            <span
                className="text-2xl font-extrabold text-white cursor-pointer mr-4 select-none"
                onClick={() => setActiveTab('shorten')}
            >
              SwiftURL
            </span>
              <button onClick={() => setActiveTab('shorten')} className={navBtn('shorten')}>
                Home
              </button>
              {token && (
                  <button
                      onClick={() => {
                        setActiveTab('links');
                        fetchUserLinks();
                      }}
                      className={navBtn('links')}
                  >
                    Links
                  </button>
              )}
            </div>

            {/* Right: Login / Logout */}
            {token ? (
                <button
                    onClick={handleLogout}
                    className="bg-white text-red-600 px-4 py-2 rounded-lg font-semibold hover:bg-red-50 transition"
                >
                  Logout
                </button>
            ) : (
                <button
                    onClick={() => setActiveTab('auth')}
                    className="bg-white text-black px-4 py-2 rounded-lg font-semibold hover:bg-indigo-50 transition"
                >
                  Login / Register
                </button>
            )}
          </div>
        </header>

        {/* Main Container (everything centered) */}
        <main className="w-full max-w-xl mx-auto mt-12 mb-12 p-8 bg-white rounded-xl shadow-md text-center">
          {/* TAB 1: Shorten URL */}
          {activeTab === 'shorten' && (
              <div className="space-y-6">
                <h2 className="text-2xl font-bold text-gray-900">Shorten a long URL</h2>
                <p className="text-sm text-gray-500">
                  Paste your long link below to generate a short, shareable link instantly.
                </p>

                {!token && (
                    <div className="p-3 bg-yellow-50 border border-yellow-200 rounded-lg text-yellow-800 text-sm">
                      Note: You are currently not logged in. Make sure to log in before shortening links!
                    </div>
                )}

                <form onSubmit={handleShortenSubmit} className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700">Long URL</label>
                    <input
                        type="url"
                        value={originalUrl}
                        onChange={(e) => setOriginalUrl(e.target.value)}
                        required
                        placeholder="https://example.com/very-long-url"
                        className={inputClass}
                    />
                  </div>
                  <button
                      type="submit"
                      className="w-full bg-black text-white py-2 rounded-lg hover:bg-indigo-700 transition font-medium"
                  >
                    Shorten URL
                  </button>
                </form>

                {shortenResult && (
                    <div className="p-4 bg-green-50 border border-green-200 rounded-lg text-green-800 text-sm space-y-2">
                      <p>
                        <b>Success!</b> Short code:{' '}
                        <span className="font-mono bg-green-100 px-1 py-0.5 rounded">
                    {shortenResult.shortCode}
                  </span>
                      </p>
                      <p>
                        Redirect link:{' '}
                        <a
                            href={`${API}/s/${shortenResult.shortCode}`}
                            target="_blank"
                            rel="noreferrer"
                            className="underline text-black"
                        >
                          {API}/s/{shortenResult.shortCode}
                        </a>
                      </p>
                    </div>
                )}
              </div>
          )}

          {/* TAB 2: Login / Register */}
          {activeTab === 'auth' && (
              <div className="space-y-6">
                <div className="flex border-b border-gray-200">
                  <button
                      onClick={() => setAuthMode('login')}
                      className={`w-1/2 py-2 text-center font-bold ${
                          authMode === 'login' ? 'text-indigo-600 border-b-2 border-indigo-600' : 'text-gray-500'
                      }`}
                  >
                    Login
                  </button>
                  <button
                      onClick={() => setAuthMode('register')}
                      className={`w-1/2 py-2 text-center font-bold ${
                          authMode === 'register' ? 'text-indigo-600 border-b-2 border-indigo-600' : 'text-gray-500'
                      }`}
                  >
                    Register
                  </button>
                </div>

                <form onSubmit={handleAuthSubmit} className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700">Email Address</label>
                    <input
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                        placeholder="you@example.com"
                        className={inputClass}
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700">Password</label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                        placeholder="••••••••"
                        className={inputClass}
                    />
                  </div>
                  <button
                      type="submit"
                      className="w-full bg-indigo-600 text-white py-2 rounded-lg hover:bg-indigo-700 transition font-medium"
                  >
                    {authMode === 'login' ? 'Login' : 'Register Account'}
                  </button>
                </form>

                {authMessage.text && (
                    <div
                        className={`p-4 rounded-lg text-sm ${
                            authMessage.isError ? 'bg-red-50 text-red-800' : 'bg-green-50 text-green-800'
                        }`}
                    >
                      {authMessage.text}
                    </div>
                )}
              </div>
          )}

          {/* TAB 3: Links */}
          {activeTab === 'links' && (
              <div className="space-y-4">
                <h2 className="text-2xl font-bold text-gray-900">Your Shortened Links</h2>
                {userLinks.length === 0 ? (
                    <p className="text-gray-500">You don't have any shortened links.</p>
                ) : (
                    userLinks.map((link) => (
                        <div
                            key={link.id || link.shortCode}
                            className="p-4 border rounded-lg shadow-sm space-y-1"
                        >
                          <p className="text-sm text-black">
                            <b>Long:</b>{' '}
                            <a href={`${link.originalUrl}`}
                            target="_blank"
                            rel="noreferrer"
                            className="underline hover:text-black"
                            >
                              {link.originalUrl || link.url}</a>
                          </p>
                          <p className="text-sm text-black">
                            <b>Short:</b>{' '}
                            <a
                                href={`http://localhost:8080/s/${link.shortCode}`}
                                target="_blank"
                                rel="noreferrer"
                                className="underline hover:text-black"
                            >
                              http://localhost:8080/s/{link.shortCode}
                            </a>
                          </p>
                        </div>
                    ))
                )}
              </div>
          )}
        </main>
      </div>
  );
}