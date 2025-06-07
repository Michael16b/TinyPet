import { Component, OnInit } from '@angular/core';
import { ApiService } from '../services/api.service';
import { UserService } from '../services/user.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import {DatePipe, NgClass, NgForOf, NgIf} from '@angular/common';
import {MatChip, MatChipSet} from '@angular/material/chips';
import {Router} from '@angular/router';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-petition',
  templateUrl: './petition.component.html',
  imports: [
    DatePipe,
    MatChip,
    MatChipSet,
    NgClass,
    NgForOf,
    NgIf,
    FormsModule
  ],
  styleUrls: ['./petition.component.css']
})
export class PetitionComponent implements OnInit {
  petitions: any[] = [];
  nextCursor: string | null = null;
  prevCursor: string | null = null; // For future use
  loading: boolean = false;
  error: string = '';
  chipsColor: string = 'primary';
  expandedCardIds: Set<string> = new Set();

  sortBy: string = 'signatureCount';
  sortOrder: string = 'desc';
  tagFilter: string = '';

  nextPageIsEmpty: boolean = false;
  private nextPageCache: any[] = [];
  cursorStack: string[] = [''];
  currentCursorIndex: number = 0;

  constructor(
    private apiService: ApiService,
    private userService: UserService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  async ngOnInit() {
    await this.loadPetitions();
  }

  async loadPetitions(cursor?: string, fromNavigationButton: boolean = false) {
    this.loading = true;
    this.error = '';
    this.nextPageIsEmpty = false;
    const limit = 4;

    try {
      const accessToken = await this.userService.getAccessToken();
      const response = await this.apiService.getPetitionList(
        accessToken,
        limit,
        cursor,
        this.sortBy,
        this.sortOrder,
        this.tagFilter?.trim() || undefined
      );

      this.petitions = response.entities || [];
      this.nextCursor = response.nextCursor || null;

      if (this.nextCursor) {
        const nextPageResp = await this.apiService.getPetitionList(
          accessToken,
          limit,
          this.nextCursor,
          this.sortBy,
          this.sortOrder,
          this.tagFilter?.trim() || undefined
        );
        this.nextPageCache = nextPageResp.entities || [];
        this.nextPageIsEmpty = this.nextPageCache.length === 0;
      } else {
        this.nextPageCache = [];
        this.nextPageIsEmpty = true;
      }
    } catch (e: any) {
      this.error = 'Impossible de charger les pétitions.';
    } finally {
      this.loading = false;
    }
  }

  async onNext() {
    if (this.nextCursor && !this.nextPageIsEmpty) {
      await this.loadPetitions(this.nextCursor, true);
    }
  }

  async onPrev() {
    if (this.currentCursorIndex > 0) {
      this.currentCursorIndex--;
      const prevCursor = this.cursorStack[this.currentCursorIndex];
      await this.loadPetitions(prevCursor, true);
    }
  }

  setSort(sortBy: string, sortOrder: string) {
    if (this.sortBy !== sortBy || this.sortOrder !== sortOrder) {
      this.sortBy = sortBy;
      this.sortOrder = sortOrder;
      this.loadPetitions();
    }
  }

  async onTagSearch() {
    await this.loadPetitions();
  }

  async clearTagFilter() {
    this.tagFilter = '';
    await this.loadPetitions();
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

  async signPetition(petition: any) {
    try {
      const accessToken = this.userService.getAccessToken();

      await this.apiService.signPetition(accessToken, petition.id);

      await this.loadPetitions();
    } catch {
    }
  }


}
