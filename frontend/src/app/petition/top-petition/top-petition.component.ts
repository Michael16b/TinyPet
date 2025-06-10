import { Component, OnInit } from '@angular/core';
import {ApiService} from '../../services/api.service';
import {Router} from '@angular/router';
import {DatePipe, NgClass, NgFor, NgIf} from '@angular/common';
import {MatChip, MatChipSet} from '@angular/material/chips';
import {UserService} from '../../services/user.service';

@Component({
  selector: 'app-top-petition',
  templateUrl: './top-petition.component.html',
  imports: [
    DatePipe,
    MatChipSet,
    MatChip,
    NgClass,
    NgIf,
    NgFor
  ],
  styleUrls: ['./top-petition.component.css']
})
export class TopPetitionComponent implements OnInit {
  petitions: any[] = [];
  loading = false;
  error = '';
  sortOrder: 'asc' | 'desc' = 'desc';
  expandedCardIds: Set<string> = new Set();

  constructor(private apiService: ApiService, private userService: UserService, private router: Router) {}

  goHome(): void {
    this.router.navigate(['/home']);
  }
  async ngOnInit() {
    await this.loadTopPetitions();
  }

  async loadTopPetitions() {
    this.loading = true;
    this.error = '';
    try {
      const accessToken = await this.userService.getAccessToken();
      const response = await this.apiService.getPetitionList(
        accessToken,
        100,
        undefined,
        'creationDate',
        this.sortOrder
      );
      this.petitions = response.entities || [];
    } catch (e) {
      this.error = "Impossible de charger le top 100.";
    } finally {
      this.loading = false;
    }
  }

  setSortOrder(order: 'asc' | 'desc') {
    if (this.sortOrder !== order) {
      this.sortOrder = order;
      this.loadTopPetitions();
    }
  }

  toggleExpand(petitionId: string) {
    if (this.expandedCardIds.has(petitionId)) {
      this.expandedCardIds.delete(petitionId);
    } else {
      this.expandedCardIds.add(petitionId);
    }
  }

  showMoreDetails(petition: any) {
    this.router.navigate(['/petition', petition.id], { state: { petition } });
  }
}
