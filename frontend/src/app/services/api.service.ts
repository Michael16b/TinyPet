import { Injectable } from '@angular/core';
import {UserService} from './user.service';
import {MatSnackBar} from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  private baseUrl: string = "https://tinypet-atalla-besily-jan.ew.r.appspot.com";
  //private baseUrl: string = "http://localhost:8080";

  constructor(
    private snackBar: MatSnackBar
  ) {}

  getBaseUrl(): string {
    return this.baseUrl;
  }

  setBaseUrl(url: string): void {
    this.baseUrl = url;
  }

  async sendPetition(petitionData: any): Promise<any> {
    try {
      const response = await fetch(`${this.baseUrl}/_ah/api/petitionApi/v1/create`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(petitionData),
      });
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      return await response.json();
    } catch (error) {
      console.error('There was a problem with the fetch operation:', error);
      throw error;
    }
  }

  async getPetitionList(
    accessToken: string,
    limit: number,
    cursor?: string,
    sortBy?: string,
    sortOrder?: string,
    tag?: string,
    userEmail?: string,
    userSearch?: string,
    userSearchField?: string,
    signedByUserEmail?: string
  ): Promise<any> {
    let url = `${this.baseUrl}/_ah/api/petitionApi/v1/list?access_token=${encodeURIComponent(accessToken)}&limit=${encodeURIComponent(limit)}`;
    if (sortBy) {
      url += `&sortBy=${encodeURIComponent(sortBy)}`;
    }
    if (sortOrder) {
      url += `&sortOrder=${encodeURIComponent(sortOrder)}`;
    }
    if (tag) {
      url += `&tag=${encodeURIComponent(tag)}`;
    }
    if (signedByUserEmail) {
      url += `&signedByUserEmail=${encodeURIComponent(signedByUserEmail)}`;
    }
    if (userEmail && !signedByUserEmail) {
      url += `&userEmail=${encodeURIComponent(userEmail)}`;
    }
    if (userSearch) {
      url += `&userSearch=${encodeURIComponent(userSearch)}`;
    }
    if (userSearchField) {
      url += `&userSearchField=${encodeURIComponent(userSearchField)}`;
    }
    if (cursor) {
      url += `&cursor=${encodeURIComponent(cursor)}`;
    }

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${accessToken}`
      }
    });
    if (!response.ok) {
      const {status, data} = await this.getMessage(response);
      this.displayError(data?.message || 'Erreur inconnue', status.toString());
      throw new Error(data?.message);
    }
    return await response.json();
  }

  async signPetition(accessToken: string, petitionId: string): Promise<any> {
    const url = `${this.baseUrl}/_ah/api/petitionApi/v1/sign?petitionId=${encodeURIComponent(petitionId)}&access_token=${encodeURIComponent(accessToken)}`;
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      }
    });

    let data: any = null;
    if (!response.ok) {
      const {status, data} = await this.getMessage(response);
      this.displayError(data?.message || 'Erreur inconnue', status.toString());
    }
    this.snackBar.open('Merci pour votre signature !', '', { duration: 2000 });
    return data;
  }

  async getSigners(accessToken: string, petitionId: string): Promise<any[]> {
    const url = `${this.baseUrl}/_ah/api/petitionApi/v1/petition/${petitionId}/signers/?access_token=${encodeURIComponent(accessToken)}`;
    const response = await fetch(url, { headers: { 'Content-Type': 'application/json' } });
    const data = await response.json();
    return data.signers;
  }

  async displayError(message: string, statut : string): Promise<void> {
    const error = new Error(message);
    (error as any).httpStatus = statut;
    this.snackBar.open(message, '', {duration: 2000});
    throw error;
  }

  async getMessage(response: Response): Promise<{ status: number, data: any }> {
    let data: any = await response.json().catch(() => null);
    return { status: response.status, data: data?.error };
  }
}
