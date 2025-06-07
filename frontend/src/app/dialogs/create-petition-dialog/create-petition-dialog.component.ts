import {ChangeDetectionStrategy, Component, computed, inject, model, OnInit, signal} from '@angular/core';
import {
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from '@angular/material/dialog';
import {FormsModule} from '@angular/forms';
import {MatButton} from '@angular/material/button';
import {
  MatDatepicker,
  MatDatepickerInput,
  MatDatepickerModule,
  MatDatepickerToggle
} from '@angular/material/datepicker';
import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {MatFormField, MatInput, MatLabel, MatSuffix} from '@angular/material/input';
import {
  MatAutocomplete,
  MatAutocompleteSelectedEvent,
  MatAutocompleteTrigger,
  MatOption
} from '@angular/material/autocomplete';
import {MatChip, MatChipGrid, MatChipInput, MatChipInputEvent, MatChipRow, MatChipsModule} from '@angular/material/chips';
import {UserService} from '../../services/user.service';
import {LiveAnnouncer} from '@angular/cdk/a11y';
import {MatIcon, MatIconModule} from '@angular/material/icon';
import {MatDialog} from '@angular/material/dialog';
import { MustLoginDialogComponent } from '../../dialogs/must-login-dialog/must-login-dialog.component';
import { MatFormFieldModule } from '@angular/material/form-field';



@Component({
  selector: 'app-create-petition-dialog',
  imports: [
    FormsModule,
    MatDialogActions,
    MatButton,
    MatDialogClose,
    MatDialogContent,
    MatDialogTitle,
    MatFormField,
    MatInput,
    MatAutocomplete,
    MatOption,
    MatAutocompleteTrigger,
    MatChipInput,
    MatChipGrid,
    MatChipRow,
    MatLabel,
    MatDatepickerModule,
    MatIcon
  ],
  templateUrl: './create-petition-dialog.component.html',
  styleUrl: './create-petition-dialog.component.css'
})
export class CreatePetitionDialogComponent {
  title = '';
  description = '';
  petitionDate: Date = new Date();

  // Gestion des tags
  selectedTags: string[] = [];
  tagInput: string = '';
  separatorKeysCodes: number[] = [ENTER, COMMA];

  readonly currentTags = model('');
  readonly tags = signal([] as string[]);
  readonly allTags: string[] = ['Environnement', 'Société', 'Animaux', 'Santé', 'Éducation', 'Écologie', 'Ville', 'Solidarité', 'Droit', 'Politique'];
  readonly filteredTags = computed(() => {
    const query = this.currentTags().toLowerCase();
    // On part d'une liste filtrée par la saisie
    let results = query
      ? this.allTags.filter(tag => tag.toLowerCase().includes(query))
      : this.allTags.slice();

    // Exclure les tags déjà sélectionnés
    results = results.filter(tag => !this.tags().includes(tag));

    return results;
  });

  readonly announcer = inject(LiveAnnouncer);



  constructor(private dialog: MatDialog, public dialogRef: MatDialogRef<CreatePetitionDialogComponent>, public userService: UserService) {}
  onCancel(): void {
    this.dialogRef.close();
  }

  // onCreate(): void {
  //   this.dialogRef.close({ title: this.title, content: this.description, tags : this.tags(), access_token: this.userService.getAccessToken() });
  // }

  openCreatePetition(): void {
    if (!this.userService.isUserLoggedIn()) {
      const mustLoginRef = this.dialog.open(MustLoginDialogComponent, {
        width: '400px',
        data: {
          title: this.title,
          description: this.description,
          tags: this.tags()
        }
      });

      mustLoginRef.afterClosed().subscribe((result) => {
        if (result && result.loggedIn) {
          this.dialogRef.close({
            title: result.title,
            content: result.description,
            tags: result.tags,
            access_token: this.userService.getAccessToken()
          });
        }
      });
      return;
    }

    this.dialogRef.close({
      title: this.title,
      content: this.description,
      tags: this.tags(),
      access_token: this.userService.getAccessToken()
    });
  }


  add(event: MatChipInputEvent): void {
    const value = (event.value || '').trim();

    if (value && !this.tags().includes(value)) {
      this.tags.update(tags => [...tags, value]);
    }

    // Réinitialiser l'input
    this.currentTags.set('');
  }

  remove(fruit: string): void {
    this.tags.update(tags => {
      const index = tags.indexOf(fruit);
      if (index < 0) {
        return tags;
      }

      tags.splice(index, 1);
      this.announcer.announce(`Removed ${fruit}`);
      return [...tags];
    });
  }

  selected(event: MatAutocompleteSelectedEvent): void {
    this.tags.update(tags => [...tags, event.option.viewValue]);
    this.currentTags.set('');
    event.option.deselect();
  }
}
