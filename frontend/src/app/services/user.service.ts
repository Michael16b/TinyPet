import { Injectable } from '@angular/core';



@Injectable({
  providedIn: 'root',
})
export class UserService {
  private loginCallback: (() => void) | null = null;
  private isLoggedIn: boolean = false;
  private tokenCheckStarted: boolean = false;

  constructor() {}

  setLoginCallback(cb: () => void) {
    this.loginCallback = cb;
  }

  login(id: string, access_token: string, name: string, family_name: string, picture: string, email: string): void {
    localStorage.setItem('id_user', id);
    localStorage.setItem('access_token', access_token);
    localStorage.setItem('name', name);
    localStorage.setItem('family_name', family_name);
    localStorage.setItem('picture', picture);
    localStorage.setItem('email', email);
    this.isLoggedIn = true;
    if (this.loginCallback) {
      this.loginCallback();  // prévenir que la connexion a eu lieu
    }
  }

  logout(): void {
    localStorage.removeItem('id_user');
    localStorage.removeItem('access_token');
    localStorage.removeItem('name');
    localStorage.removeItem('family_name');
    localStorage.removeItem('picture');
    localStorage.removeItem('email');

    this.isLoggedIn = false;
    this.tokenCheckStarted = false;
  }


  getIdUser(): string | null {
    return localStorage.getItem('id_user');
  }

  getIsLoggedIn(): boolean {
    return this.isLoggedIn;
  }

  getAccessToken(): string {
    return <string>localStorage.getItem('access_token');
  }

  getName(): string {
    return localStorage.getItem('name') || "";
  }

  getFamilyName(): string {
    return localStorage.getItem('family_name') || "";
  }

  getPicture(): string | null {
    return localStorage.getItem('picture') || "";
  }

  getEmail(): string | null {
    return localStorage.getItem('email');
  }

  isUserLoggedIn(): boolean {
    const idUser = localStorage.getItem('id_user');
    const username = localStorage.getItem('name');
    const email = localStorage.getItem('email');

    return !!(idUser && username && email);
  }

  refreshUser(): void {
    this.isLoggedIn = this.isUserLoggedIn();


    // Lancer la surveillance du token seulement si connecté
    if (this.isLoggedIn && !this.tokenCheckStarted) {
      this.tokenCheckStarted = true;

      setInterval(() => {
        const currentToken = this.getAccessToken();

        if (!currentToken || this.isTokenExpired(currentToken)) {
          console.log('⚠️ Token expiré, on déconnecte');
          this.logout();
        }
      }, 5_000);
    }
  }

  isTokenExpired(token: string): boolean {
    if (!token) return true;
    const payload = JSON.parse(atob(token.split('.')[1]));
    const now = Math.floor(Date.now() / 1000);
    return payload.exp < now;
  }

}
