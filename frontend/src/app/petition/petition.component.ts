import { Component, OnInit } from '@angular/core';
import { ApiService } from '../services/api.service';
import { UserService } from '../services/user.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import {DatePipe, NgClass, NgForOf, NgIf} from '@angular/common';
import {MatChip, MatChipSet} from '@angular/material/chips';
import {Router} from '@angular/router';

@Component({
  selector: 'app-petition',
  templateUrl: './petition.component.html',
  imports: [
    DatePipe,
    MatChip,
    MatChipSet,
    NgClass,
    NgForOf,
    NgIf
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
      const response = await this.apiService.getPetitionList(accessToken, limit, cursor);

      this.petitions = response.entities || [];
      this.nextCursor = response.nextCursor || null;

      // Gestion du stack de cursors
      if (!fromNavigationButton) {
        this.cursorStack = [''];
        this.currentCursorIndex = 0;
      } else if (cursor !== undefined && this.cursorStack[this.currentCursorIndex] !== cursor) {
        // Si on navigue vers une page déjà dans la pile, on ajuste l'index
        const existingIdx = this.cursorStack.indexOf(cursor);
        if (existingIdx !== -1) {
          this.currentCursorIndex = existingIdx;
        } else {
          // Sinon, on ajoute le cursor (page suivante)
          this.cursorStack.push(cursor);
          this.currentCursorIndex = this.cursorStack.length - 1;
        }
      }

      // Précharge la prochaine page si possible
      if (this.nextCursor) {
        const nextPageResp = await this.apiService.getPetitionList(accessToken, limit, this.nextCursor);
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
