import { Injectable } from '@angular/core';



@Injectable({
  providedIn: 'root',
})
export class UserService {
  private loginCallback: (() => void) | null = null;



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
  }


  getIdUser(): string | null {
    return localStorage.getItem('id_user');
  }

  getAccessToken(): string {
    return <string>localStorage.getItem('access_token');
  }

  getName(): string | null {
    return localStorage.getItem('name');
  }

  getFamilyName(): string | null {
    return localStorage.getItem('family_name');
  }

  getPicture(): string | null {
    return localStorage.getItem('picture');
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

}
