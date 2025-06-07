import { Component, OnInit } from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {UserService} from '../../services/user.service';
import {ApiService} from '../../services/api.service';
import {DatePipe, NgForOf, NgIf} from '@angular/common';

@Component({
  selector: 'app-petition-details',
  templateUrl: './petition-details.component.html',
  imports: [
    DatePipe,
    NgIf,
    NgForOf
  ],
  styleUrls: ['./petition-details.component.css']
})
export class PetitionDetailsComponent implements OnInit {
  petition: any;
  signers: any[] = [];
  loading = true;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private apiService: ApiService,
    private userService: UserService
  ) {}

  async ngOnInit() {
    this.loading = true;
    let petitionId: string | null = this.route.snapshot.paramMap.get('id');
    this.petition = history.state?.petition;

    if (!this.petition || !petitionId) {
      this.error = 'Aucun identifiant de pétition fourni ou données de pétition non disponibles';
      this.loading = false;
      return;
    }

    try {
      const accessToken = await this.userService.getAccessToken();
      this.signers = await this.apiService.getSigners(accessToken, petitionId);
    } catch (e) {
      this.error = 'Impossible de charger les signataires de la pétition';
    } finally {
      this.loading = false;
    }
  }
}
