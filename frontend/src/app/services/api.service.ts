import { Injectable } from '@angular/core';
import {UserService} from './user.service';
import {MatSnackBar} from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  // private baseUrl: string = window.location.protocol + "//" + window.location.host; // For production
  private baseUrl: string = "https://tinypet-atalla-besily-jan.ew.r.appspot.com"; // For pre-production
  //private baseUrl: string = "http://localhost:8080"; // For local development

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
      this.displayError(response)
      throw new Error('Failed to fetch petition list');
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
    console.log("Signing petition with URL: ", url);

    let data: any = null;
    if (!response.ok) {
      this.displayError(response);
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

  async displayError(response: Response): Promise<void> {
    let data: any = await response.json().catch(() => null);
    const message = data?.error?.message || data?.message || 'Failed to sign petition';
    const error = new Error(message);
    (error as any).httpStatus = response.status;
    this.snackBar.open(message, '', {duration: 2000});
    throw error;
  }
}
