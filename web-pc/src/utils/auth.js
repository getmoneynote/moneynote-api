/**
 * 登录态 token 的存取。
 * 记住我：存 localStorage；不记住：存 sessionStorage。
 * 读取时优先 localStorage，回退到 sessionStorage。
 */
const TOKEN_KEY = 'accessToken';

export const getAccessToken = () =>
  localStorage.getItem(TOKEN_KEY) ?? sessionStorage.getItem(TOKEN_KEY);

export const setAccessToken = (token, remember) => {
  if (remember) {
    localStorage.setItem(TOKEN_KEY, token);
    sessionStorage.removeItem(TOKEN_KEY);
  } else {
    sessionStorage.setItem(TOKEN_KEY, token);
    localStorage.removeItem(TOKEN_KEY);
  }
};

export const clearAccessToken = () => {
  localStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(TOKEN_KEY);
};
